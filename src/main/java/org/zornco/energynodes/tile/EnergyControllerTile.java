package org.zornco.energynodes.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EnergyControllerTile extends TileEntity implements ITickableTileEntity {

    protected static final String NBT_CONNECTED_INPUT_NODES_KEY = "connected-input-nodes";
    protected static final String NBT_CONNECTED_OUTPUT_NODES_KEY = "connected-output-nodes";
    protected static final String NBT_TOTAL_ENERGY_TRANSFERRED_KEY = "total-energy-transferred";
    protected static final String NBT_RATE_LIMIT_KEY = "rate-limit";

    public static final int UNLIMITED_RATE = -1;
    protected int rateLimit = UNLIMITED_RATE;

    protected int ticks = 0;
    protected long totalEnergyTransferred = 0;
    protected long totalEnergyTransferredLastTick = 0;
    protected float transferRate = 0;
    protected float lastTransferRateSent = 0;
    protected int ticksSinceLastTransferRatePacket = 0;

    public List<BlockPos> connectedInputNodes = new ArrayList<>();
    public List<BlockPos> connectedOutputNodes = new ArrayList<>();
    public long transferredThisTick;


    public EnergyControllerTile() {
        super(Registration.ENERGY_CONTROLLER_TILE.get());
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT tag) {
        super.read(state, tag);
        Utils.LBPCODEC.decode(NBTDynamicOps.INSTANCE, tag.getList(NBT_CONNECTED_INPUT_NODES_KEY, Constants.NBT.TAG_INT_ARRAY))
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(listINBTPair -> this.connectedInputNodes = new ArrayList<>(listINBTPair.getFirst()));
        Utils.LBPCODEC.decode(NBTDynamicOps.INSTANCE, tag.getList(NBT_CONNECTED_OUTPUT_NODES_KEY, Constants.NBT.TAG_INT_ARRAY))
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(listINBTPair -> this.connectedOutputNodes = new ArrayList<>(listINBTPair.getFirst()));
        this.totalEnergyTransferred = tag.getLong(NBT_TOTAL_ENERGY_TRANSFERRED_KEY);
        this.rateLimit = tag.getInt(NBT_RATE_LIMIT_KEY);

        this.totalEnergyTransferredLastTick = this.totalEnergyTransferred;
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        CompoundNBT tag = super.write(compound);
        Utils.LBPCODEC.encodeStart(NBTDynamicOps.INSTANCE, connectedInputNodes)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(inbt -> tag.put(NBT_CONNECTED_INPUT_NODES_KEY, inbt));
        Utils.LBPCODEC.encodeStart(NBTDynamicOps.INSTANCE, connectedOutputNodes)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(inbt -> tag.put(NBT_CONNECTED_OUTPUT_NODES_KEY, inbt));
        tag.putLong(NBT_TOTAL_ENERGY_TRANSFERRED_KEY, this.totalEnergyTransferred);
        tag.putInt(NBT_RATE_LIMIT_KEY, this.rateLimit);
        return tag;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();
        Utils.LBPCODEC.encodeStart(NBTDynamicOps.INSTANCE, connectedInputNodes)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(inbt -> tag.put(NBT_CONNECTED_INPUT_NODES_KEY, inbt));
        Utils.LBPCODEC.encodeStart(NBTDynamicOps.INSTANCE, connectedOutputNodes)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(inbt -> tag.put(NBT_CONNECTED_OUTPUT_NODES_KEY, inbt));
        tag.putLong(NBT_TOTAL_ENERGY_TRANSFERRED_KEY, this.totalEnergyTransferred);
        tag.putInt(NBT_RATE_LIMIT_KEY, this.rateLimit);
        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        Utils.LBPCODEC.decode(NBTDynamicOps.INSTANCE, tag.getList(NBT_CONNECTED_INPUT_NODES_KEY, Constants.NBT.TAG_INT_ARRAY))
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(listINBTPair -> this.connectedInputNodes = new ArrayList<>(listINBTPair.getFirst()));
        Utils.LBPCODEC.decode(NBTDynamicOps.INSTANCE, tag.getList(NBT_CONNECTED_OUTPUT_NODES_KEY, Constants.NBT.TAG_INT_ARRAY))
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(listINBTPair -> this.connectedOutputNodes = new ArrayList<>(listINBTPair.getFirst()));
        this.totalEnergyTransferred = tag.getLong(NBT_TOTAL_ENERGY_TRANSFERRED_KEY);
        this.rateLimit = tag.getInt(NBT_RATE_LIMIT_KEY);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 1, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet){
        if (this.getWorld() != null) {
            this.handleUpdateTag(this.getWorld().getBlockState(pos), packet.getNbtCompound());
            ModelDataManager.requestModelDataRefresh(this);
            this.getWorld().markBlockRangeForRenderUpdate(this.pos, this.getBlockState(), this.getBlockState());
        }
    }

    public boolean canReceiveEnergy(EnergyNodeTile nodeTile) {
        return this.connectedInputNodes.contains(nodeTile.getPos()) || this.connectedOutputNodes.contains(nodeTile.getPos());
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (Objects.requireNonNull(this.world).isRemote) {
            return 0;
        }
        int amountReceived = 0;

        float connectedEnergyTilesAmount;
        if (!simulate) {
            connectedEnergyTilesAmount = this.connectedOutputNodes
                    .stream()
                    .map(blockPos -> (EnergyNodeTile) world.getTileEntity(blockPos))
                    .filter(Objects::nonNull)
                    .mapToInt(outputTile -> outputTile.connectedTiles
                            .entrySet()
                            .stream()
                            .mapToInt(entry ->
                                entry.getValue()
                                        .getCapability(CapabilityEnergy.ENERGY,
                                                getFacingFromBlockPos(outputTile.getPos(), entry.getKey()))
                                        .map(iEnergyStorage -> (iEnergyStorage.canReceive() ||
                                        iEnergyStorage.getEnergyStored() / iEnergyStorage.getMaxEnergyStored() != 1)
                                        ? 1 : 0).orElse(0))
                            .sum())
                    .sum();
        } else {
            connectedEnergyTilesAmount = 1;
        }


        for (BlockPos outputBlockPos : this.connectedOutputNodes) {
            EnergyNodeTile outputTile = (EnergyNodeTile) world.getTileEntity(outputBlockPos);
            if (outputTile != null) {
                for (Map.Entry<BlockPos, TileEntity> tileEntry : outputTile.connectedTiles.entrySet()) {
                    BlockPos outputOffset = tileEntry.getKey();
                    Direction facing = getFacingFromBlockPos(outputBlockPos, outputOffset);
                    TileEntity otherTile = tileEntry.getValue();
                    int amountReceivedThisBlock = 0;
                    if (otherTile != null && !(otherTile instanceof EnergyNodeTile)) {
                        LazyOptional<IEnergyStorage> adjacentStorageOptional = otherTile.getCapability(CapabilityEnergy.ENERGY, facing);
                        if (adjacentStorageOptional.isPresent()) {
                            IEnergyStorage adjacentStorage = adjacentStorageOptional.orElseThrow(
                                    () -> new RuntimeException("Failed to get present adjacent storage for pos " + this.pos));
                            int amountToSend = (int) ((this.rateLimit == UNLIMITED_RATE ? maxReceive : Math.min(maxReceive, this.rateLimit))/connectedEnergyTilesAmount);
                            amountReceivedThisBlock = adjacentStorage.receiveEnergy(amountToSend, simulate);
                        }
                    }

                    if (!simulate) {
                        this.totalEnergyTransferred += amountReceivedThisBlock;
                    }
                    amountReceived += amountReceivedThisBlock;
                }
            }
        }
        return amountReceived;
    }

    @Nonnull
    private Direction getFacingFromBlockPos(BlockPos pos, BlockPos neighbor) {
        return Direction.getFacingFromVector(
                (float) (pos.getX() - neighbor.getX()),
                (float) (pos.getY() - neighbor.getY()),
                (float) (pos.getZ() - neighbor.getZ()));
    }

    @Override
    public void tick() {
        if (this.world != null && this.world.isRemote) {
            return;
        }

        // Compute the FE transfer in this tick by taking the difference between total transfer this
        // tick and the total transfer last tick
        transferredThisTick = Math.abs(this.totalEnergyTransferred - this.totalEnergyTransferredLastTick);

        // Add FE transfer this tick to moving average
        //this.transferRateMovingAverage.add(transferredThisTick);
        //this.transferRate = this.transferRateMovingAverage.getAverage(); // compute average FE/t
        this.totalEnergyTransferredLastTick = this.totalEnergyTransferred;

        if (transferredThisTick > 0) {
            this.markDirty();
        }

        // Send update packet to all nearby players if required (if the transfer rate changed or enough
        // ticks have passed since the last packet)
        /*if (((this.transferRate != this.lastTransferRateSent || this.transferRate > 0)
                && this.ticksSinceLastTransferRatePacket > UPDATE_PACKET_MIN_TICK_INTERVAL)
                || this.ticksSinceLastTransferRatePacket >= UPDATE_PACKET_MAX_TICK_INTERVAL) {

            this.lastTransferRateSent = this.transferRate;
            this.ticksSinceLastTransferRatePacket = 0;

            EnergyMetersMod.NETWORK.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(this.pos)),
                    new PacketEnergyTransferRate(this.pos, this.transferRate, this.totalEnergyTransferred));
        } else {
            this.ticksSinceLastTransferRatePacket++;
        }*/

        if (this.ticks % 10 == 0) {
            //this.checkConnections();
        }

        this.ticks = ++this.ticks % 20;
    }
}
