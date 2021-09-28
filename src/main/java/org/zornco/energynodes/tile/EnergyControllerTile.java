package org.zornco.energynodes.tile;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.capability.NodeEnergyStorage;
import org.zornco.energynodes.item.EnergyLinkerItem;
import org.zornco.energynodes.nbt.NbtListCollector;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class EnergyControllerTile extends TileEntity implements ITickableTileEntity {

    protected static final String NBT_CONNECTED_NODES_KEY = "connected-nodes";
    protected static final String NBT_CONNECTED_INPUT_NODES_KEY = "connected-input-nodes";
    protected static final String NBT_CONNECTED_OUTPUT_NODES_KEY = "connected-output-nodes";
    protected static final String NBT_TOTAL_ENERGY_TRANSFERRED_KEY = "total-energy-transferred";
    protected static final String NBT_TRANSFERRED_THIS_TICK_KEY = "transferred-this-tick";
    protected static final String NBT_RATE_LIMIT_KEY = "rate-limit";

    public static final int UNLIMITED_RATE = -1;
    protected int rateLimit = UNLIMITED_RATE;

    protected int ticks = 0;
    protected long totalEnergyTransferred = 0;
    protected long totalEnergyTransferredLastTick = 0;
    protected float transferRate = 0;
    protected float lastTransferRateSent = 0;
    protected int ticksSinceLastTransferRatePacket = 0;

    public final HashSet<BlockPos> connectedNodes = new HashSet<>();
    public final HashSet<LazyOptional<IEnergyStorage>> inputs = new HashSet<>();
    public final HashSet<LazyOptional<IEnergyStorage>> outputs = new HashSet<>();

    public long transferredThisTick;
    private int particleRate;


    public EnergyControllerTile() {
        super(Registration.ENERGY_CONTROLLER_TILE.get());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        connectedNodes.forEach(nodePos -> {
            final TileEntity tile = level.getBlockEntity(nodePos);
            if(tile instanceof EnergyNodeTile) {
                EnergyNodeTile entile = (EnergyNodeTile) tile;
                entile.controllerPos = null;
                entile.energyStorage.setController(null);
                entile.energyStorage.setEnergyStored(0);
                entile.setChanged();
            }
        });
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();

        ImmutableSet.copyOf(this.inputs).forEach(LazyOptional::invalidate);
        ImmutableSet.copyOf(this.outputs).forEach(LazyOptional::invalidate);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!level.isClientSide) {
            final MinecraftServer server = level.getServer();
            server.tell(new TickDelayedTask(server.getTickCount(), this::loadEnergyCapsFromLevel));
        }
    }

    @Override
    public void load(@Nonnull BlockState state, @Nonnull CompoundNBT tag) {
        super.load(state, tag);
        // inputPositions
        BlockPos.CODEC.listOf().fieldOf(NBT_CONNECTED_INPUT_NODES_KEY).codec()
                .parse(NBTDynamicOps.INSTANCE, tag)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(connectedNodes::addAll);

        // outputs
        BlockPos.CODEC.listOf().fieldOf(NBT_CONNECTED_OUTPUT_NODES_KEY).codec()
                .parse(NBTDynamicOps.INSTANCE, tag)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(connectedNodes::addAll);

        this.totalEnergyTransferred = tag.getLong(NBT_TOTAL_ENERGY_TRANSFERRED_KEY);
        this.transferredThisTick = tag.getLong(NBT_TRANSFERRED_THIS_TICK_KEY);
        this.rateLimit = tag.getInt(NBT_RATE_LIMIT_KEY);

        this.totalEnergyTransferredLastTick = this.totalEnergyTransferred;
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT compound) {
        CompoundNBT tag = super.save(compound);

        tag.put(NBT_CONNECTED_INPUT_NODES_KEY, getStorageNbt(inputs));
        tag.put(NBT_CONNECTED_OUTPUT_NODES_KEY, getStorageNbt(outputs));

        tag.putLong(NBT_TOTAL_ENERGY_TRANSFERRED_KEY, this.totalEnergyTransferred);
        tag.putLong(NBT_TRANSFERRED_THIS_TICK_KEY, this.transferredThisTick);
        tag.putInt(NBT_RATE_LIMIT_KEY, this.rateLimit);
        return tag;
    }

    @Nonnull
    private ListNBT getStorageNbt(Collection<LazyOptional<IEnergyStorage>> storages) {
        return storages.stream()
                .map(LazyOptional::resolve)
                .filter(Optional::isPresent)
                .map(opt -> {
                    return opt.map(storage -> {
                        if (storage instanceof NodeEnergyStorage) {
                            BlockPos pos = ((NodeEnergyStorage) storage).getLocation();
                            return BlockPos.CODEC.encodeStart(NBTDynamicOps.INSTANCE, pos).getOrThrow(false, EnergyNodes.LOGGER::error);
                        }

                        return null;
                    }).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(NbtListCollector.toNbtList());
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        load(state, tag);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 1, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        if (this.getLevel() != null) {
            this.handleUpdateTag(this.getLevel().getBlockState(worldPosition), packet.getTag());
            ModelDataManager.requestModelDataRefresh(this);
            this.getLevel().setBlocksDirty(this.worldPosition, this.getBlockState(), this.getBlockState());
        }
    }

    public boolean canReceiveEnergy(EnergyNodeTile nodeTile) {
        return this.connectedNodes.contains(nodeTile.getBlockPos());
    }

    public int receiveEnergy(EnergyNodeTile inputTile, int maxReceive, boolean simulate) {
        if (Objects.requireNonNull(this.level).isClientSide) {
            return 0;
        }
        int amountReceived = 0;

        // TODO - These should be capability references, store them!
        float connectedEnergyTilesAmount;
        if (!simulate) {
            connectedEnergyTilesAmount = this.outputs.stream().mapToInt(lazy -> {
                return lazy.map(storage -> {
                    // todo: let's cheat for now
                    EnergyNodeTile node = ((NodeEnergyStorage) storage).getNodeTile();
                    return node.connectedTiles.values()
                            // TODO: Make another cap for other Lazy storage
                            .stream()
                            .mapToInt(tile -> tile
                                    .getCapability(CapabilityEnergy.ENERGY, getFacingFromBlockPos(node.getBlockPos(), tile.getBlockPos()))
                                    .map(iEnergyStorage -> (iEnergyStorage.canReceive() ||
                                            iEnergyStorage.getEnergyStored() / iEnergyStorage.getMaxEnergyStored() != 1) ? 1 : 0)
                                    .orElse(0))
                            .sum();
                }).orElse(0);
            }).sum();
        } else {
            connectedEnergyTilesAmount = 1;
        }

        for (BlockPos outputEntry : this.connectedNodes) {
            EnergyNodeTile outputTile = (EnergyNodeTile) level.getBlockEntity(outputEntry);
            if (outputTile != null) {
                int transferredThisTile = 0;
                for (Map.Entry<BlockPos, TileEntity> tileEntry : outputTile.connectedTiles.entrySet()) {
                    BlockPos outputOffset = tileEntry.getKey();
                    Direction facing = getFacingFromBlockPos(outputEntry, outputOffset);
                    TileEntity otherTile = tileEntry.getValue();
                    int amountReceivedThisBlock = 0;
                    if (otherTile != null && !(otherTile instanceof EnergyNodeTile)) {
                        LazyOptional<IEnergyStorage> adjacentStorageOptional = otherTile.getCapability(CapabilityEnergy.ENERGY, facing);
                        if (adjacentStorageOptional.isPresent()) {
                            IEnergyStorage adjacentStorage = adjacentStorageOptional.orElseThrow(
                                    () -> new RuntimeException("Failed to get present adjacent storage for pos " + this.worldPosition));
                            int amountToSend = (int) ((this.rateLimit == UNLIMITED_RATE ? maxReceive : Math.min(maxReceive, this.rateLimit)) / connectedEnergyTilesAmount);
                            amountReceivedThisBlock = adjacentStorage.receiveEnergy(amountToSend, simulate);
                        }
                    }

                    if (!simulate) {
                        transferredThisTile += amountReceivedThisBlock;
                        this.totalEnergyTransferred += amountReceivedThisBlock;
                    }
                    amountReceived += amountReceivedThisBlock;
                }

                if (!simulate) {
                    int output = outputTile.getMovingAverage() + transferredThisTile;
                    outputTile.setMovingAverage(output);
                    outputTile.energyStorage.setEnergyStored(output);
                }
            }
        }

        if (!simulate) {
            int input = inputTile.getMovingAverage() + amountReceived;
            inputTile.setMovingAverage(input);

            inputTile.energyStorage.setEnergyStored(amountReceived);
        }

        return amountReceived;
    }

    @Nonnull
    private Direction getFacingFromBlockPos(BlockPos pos, BlockPos neighbor) {
        return Direction.getNearest(
                (float) (pos.getX() - neighbor.getX()),
                (float) (pos.getY() - neighbor.getY()),
                (float) (pos.getZ() - neighbor.getZ()));
    }

    @Override
    public void tick() {
        if (this.level == null) {
            return;
        }
        if (!this.level.isClientSide) {
            // Compute the FE transfer in this tick by taking the difference between total transfer this
            // tick and the total transfer last tick
            transferredThisTick = Math.abs(this.totalEnergyTransferred);

            // Add FE transfer this tick to moving average
            //this.transferRateMovingAverage.add(transferredThisTick);
            //this.transferRate = this.transferRateMovingAverage.getAverage(); // compute average FE/t
            //this.totalEnergyTransferredLastTick = this.totalEnergyTransferred;

            inputs.forEach(opt -> {
                opt.ifPresent(storage -> {
                    final NodeEnergyStorage s = (NodeEnergyStorage) storage;
                    s.setEnergyStored(0);
                    s.getNodeTile().setMovingAverage(0);
                });
            });

            outputs.forEach(opt -> {
                opt.ifPresent(storage -> {
                    final NodeEnergyStorage s = (NodeEnergyStorage) storage;
                    s.setEnergyStored(0);
                    s.getNodeTile().setMovingAverage(0);
                });
            });

            this.totalEnergyTransferred = 0;
            if (transferredThisTick > 0) {
                this.setChanged();
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
                this.setChanged();
                //this.checkConnections();
            }
        } else {
            if (this.ticks % 10 == 0) {

                spawnParticles();
            }
        }
        this.ticks = ++this.ticks % 20;
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticles() {
        if (connectedNodes.size() <= 0) return;
        if (Minecraft.getInstance().player != null && level != null && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof EnergyLinkerItem) {
            // TODO - Particles overhaul once caps are in
            connectedNodes.stream()
                    .filter(pos -> {
                        final BlockState state = level.getBlockState(pos);
                        return state.getBlock() instanceof EnergyNodeBlock && state.getValue(EnergyNodeBlock.PROP_INOUT) == EnergyNodeBlock.Flow.IN;
                    }).forEach(inputPos -> {
                        Vector3d spawn = Vector3d.atCenterOf(inputPos);
                        Vector3d dest = Vector3d.atCenterOf(worldPosition).subtract(spawn).scale(.1);
                        level.addParticle(ParticleTypes.END_ROD, spawn.x, spawn.y, spawn.z, dest.x, dest.y, dest.z);
                    });

            connectedNodes.stream()
                    .filter(pos -> {
                        final BlockState state = level.getBlockState(pos);
                        return state.getBlock() instanceof EnergyNodeBlock && state.getValue(EnergyNodeBlock.PROP_INOUT) == EnergyNodeBlock.Flow.OUT;
                    }).forEach(outputPos -> {
                        Vector3d spawn = Vector3d.atCenterOf(worldPosition);
                        Vector3d dest = Vector3d.atCenterOf(outputPos).subtract(spawn).scale(.1);
                        level.addParticle(ParticleTypes.END_ROD, spawn.x, spawn.y, spawn.z, dest.x, dest.y, dest.z);
                    });
        }
    }

    private void loadEnergyCapsFromLevel() {
        Set<BlockPos> invalid = new HashSet<>();
        for (BlockPos nodePos : connectedNodes) {
            // load and parse capability references
            if (level.isLoaded(nodePos)) {
                final BlockState state = level.getBlockState(nodePos);
                final TileEntity tn = level.getBlockEntity(nodePos);
                if (tn instanceof EnergyNodeTile) {
                    final LazyOptional<IEnergyStorage> cap = tn.getCapability(CapabilityEnergy.ENERGY, null);
                    if (!cap.isPresent()) {
                        invalid.add(nodePos);
                        continue;
                    }

                    switch (state.getValue(EnergyNodeBlock.PROP_INOUT)) {
                        case IN:
                            inputs.add(cap);
                            cap.addListener(removed -> {
                                this.inputs.remove(removed);
                                this.connectedNodes.remove(nodePos);
                            });
                            break;

                        case OUT:
                            outputs.add(cap);
                            cap.addListener(removed -> {
                                this.outputs.remove(removed);
                                this.connectedNodes.remove(nodePos);
                            });
                            break;

                        default:
                            break;
                    }
                }
            }
        }

        connectedNodes.removeAll(invalid);
    }
}
