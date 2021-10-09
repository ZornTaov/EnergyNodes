package org.zornco.energynodes.tile;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.RegistryObject;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.capability.NodeEnergyStorage;
import org.zornco.energynodes.item.EnergyLinkerItem;
import org.zornco.energynodes.nbt.NbtListCollector;
import org.zornco.energynodes.particles.EnergyNodeParticleData;
import org.zornco.energynodes.tiers.ControllerTier;
import org.zornco.energynodes.tiers.IControllerTier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class EnergyControllerTile extends TileEntity implements ITickableTileEntity {

    protected int ticks = 0;
    protected long totalEnergyTransferred = 0;
    protected long totalEnergyTransferredLastTick = 0;

    public final HashSet<BlockPos> connectedNodes = new HashSet<>();
    public final HashSet<LazyOptional<IEnergyStorage>> inputs = new HashSet<>();
    public final HashSet<LazyOptional<IEnergyStorage>> outputs = new HashSet<>();

    public long transferredThisTick;
    private AxisAlignedBB renderBounds;

    public IControllerTier tier;
    private LazyOptional<IControllerTier> tierLO;

    public EnergyControllerTile() {
        super(Registration.ENERGY_CONTROLLER_TILE.get());
        this.tier = new ControllerTier();
        tierLO = LazyOptional.of(() -> this.tier);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        connectedNodes.forEach(nodePos -> {
            if(level != null) {
                final TileEntity tile = level.getBlockEntity(nodePos);
                if (tile instanceof EnergyNodeTile) {
                    EnergyNodeTile enTile = (EnergyNodeTile) tile;
                    enTile.controllerPos = null;
                    enTile.energyStorage.setController(null);
                    enTile.energyStorage.setEnergyStored(0);
                    enTile.setChanged();
                }
            }
        });
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        tierLO.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level != null)
            if (!level.isClientSide) {
                final MinecraftServer server = level.getServer();
                if (server != null) {
                    server.tell(new TickDelayedTask(server.getTickCount(), this::loadEnergyCapsFromLevel));
                }
            }
            else
            {
                renderBounds = super.getRenderBoundingBox();
            }
    }

    @Override
    public void load(@Nonnull BlockState state, @Nonnull CompoundNBT tag) {
        super.load(state, tag);
        // inputPositions
        BlockPos.CODEC.listOf().fieldOf(EnergyNodeConstants.NBT_CONNECTED_INPUT_NODES_KEY).codec()
                .parse(NBTDynamicOps.INSTANCE, tag)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(connectedNodes::addAll);

        // outputs
        BlockPos.CODEC.listOf().fieldOf(EnergyNodeConstants.NBT_CONNECTED_OUTPUT_NODES_KEY).codec()
                .parse(NBTDynamicOps.INSTANCE, tag)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(connectedNodes::addAll);

        this.totalEnergyTransferred = tag.getLong(EnergyNodeConstants.NBT_TOTAL_ENERGY_TRANSFERRED_KEY);
        this.transferredThisTick = tag.getLong(EnergyNodeConstants.NBT_TRANSFERRED_THIS_TICK_KEY);
        this.tier.setTier(Registration.TIERS.getEntries()
                .stream()
                .map(RegistryObject::get)
                .filter(tier -> tier.getSerializedName().equals(tag.getString(EnergyNodeConstants.NBT_TIER)))
                .findFirst()
                .orElse(Registration.BASE.get()));
        this.totalEnergyTransferredLastTick = this.totalEnergyTransferred;
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT compound) {
        CompoundNBT tag = super.save(compound);
        checkConnections();
        tag.put(EnergyNodeConstants.NBT_CONNECTED_INPUT_NODES_KEY, getStorageNbt(inputs));
        tag.put(EnergyNodeConstants.NBT_CONNECTED_OUTPUT_NODES_KEY, getStorageNbt(outputs));

        tag.putLong(EnergyNodeConstants.NBT_TOTAL_ENERGY_TRANSFERRED_KEY, this.totalEnergyTransferred);
        tag.putLong(EnergyNodeConstants.NBT_TRANSFERRED_THIS_TICK_KEY, this.transferredThisTick);
        tag.putString(EnergyNodeConstants.NBT_TIER, this.tier.getSerializedName());
        return tag;
    }

    @Nonnull
    private ListNBT getStorageNbt(Collection<LazyOptional<IEnergyStorage>> storages) {
        return storages.stream()
                .map(LazyOptional::resolve)
                .filter(Optional::isPresent)
                .map(opt -> opt.map(storage -> {
                    if (storage instanceof NodeEnergyStorage) {
                        BlockPos pos = ((NodeEnergyStorage) storage).getLocation();
                        return BlockPos.CODEC.encodeStart(NBTDynamicOps.INSTANCE, pos).getOrThrow(false, EnergyNodes.LOGGER::error);
                    }

                    return null;
                }).orElse(null))
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
        return new SUpdateTileEntityPacket(this.worldPosition, -1, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        if (this.getLevel() != null) {
            this.handleUpdateTag(this.getLevel().getBlockState(worldPosition), packet.getTag());
            ModelDataManager.requestModelDataRefresh(this);
            this.getLevel().setBlocksDirty(this.worldPosition, this.getBlockState(), this.getBlockState());
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == Registration.TIER_CAPABILITY)
            return tierLO.cast();

        return super.getCapability(cap, side);
    }

    public boolean canReceiveEnergy(EnergyNodeTile nodeTile) {
        return this.connectedNodes.contains(nodeTile.getBlockPos());
    }

    public int receiveEnergy(EnergyNodeTile inputTile, int maxReceive, boolean simulate) {
        // TODO - double and triple check the correct amount of energy is being transferred!
        if (Objects.requireNonNull(this.level).isClientSide) {
            return 0;
        }
        int amountReceived = 0;

        // TODO - These should be capability references, store them!
        float connectedEnergyTilesAmount;
        if (!simulate) {
            connectedEnergyTilesAmount = this.outputs.stream().mapToInt(lazy -> lazy.map(storage -> {
                // TODO: let's cheat for now
                EnergyNodeTile node = ((NodeEnergyStorage) storage).getNodeTile();
                return node.connectedTiles.values()
                        // TODO: Make another cap for other Lazy storage
                        .stream()
                        .filter(Objects::nonNull)
                        .mapToInt(tile -> tile
                                .getCapability(CapabilityEnergy.ENERGY, getFacingFromBlockPos(node.getBlockPos(), tile.getBlockPos()))
                                .map(iEnergyStorage -> (iEnergyStorage.canReceive() ||
                                        iEnergyStorage.getEnergyStored() / iEnergyStorage.getMaxEnergyStored() != 1) ? 1 : 0)
                                .orElse(0))
                        .sum();
            }).orElse(0)).sum();
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
                            int amountToSend = (int) ((this.tier.getMaxTransfer() == EnergyNodeConstants.UNLIMITED_RATE ? maxReceive / connectedEnergyTilesAmount : Math.min(maxReceive, this.tier.getMaxTransfer())) / connectedEnergyTilesAmount);
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
                    int output = outputTile.energyStorage.getEnergyStored() + transferredThisTile;
                    outputTile.energyStorage.setEnergyStored(output);
                }
            }
        }

        if (!simulate) {
            inputTile.energyStorage.setEnergyStored(inputTile.energyStorage.getEnergyStored() + amountReceived);
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

            inputs.forEach(opt -> opt.ifPresent(storage -> {
                final NodeEnergyStorage s = (NodeEnergyStorage) storage;
                s.setEnergyStored(0);
            }));

            outputs.forEach(opt -> opt.ifPresent(storage -> {
                final NodeEnergyStorage s = (NodeEnergyStorage) storage;
                s.setEnergyStored(0);
            }));

            this.totalEnergyTransferred = 0;
            if (transferredThisTick > 0) {
                this.setChanged();
            }

            if (this.ticks % 10 == 0) {
                this.setChanged();
            }
        } else {
            if (this.ticks % 10 == 0) {

                this.checkConnections();
                spawnParticles();
            }
        }
        this.ticks = ++this.ticks % 20;
    }

    private void checkConnections() {
        if (connectedNodes.size() - inputs.size() - outputs.size() != 0)
            loadEnergyCapsFromLevel();
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticles() {
        if (connectedNodes.size() <= 0) return;
        if (Minecraft.getInstance().player != null && level != null && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof EnergyLinkerItem) {
            // TODO - Particles overhaul once caps are in
            // TODO - Maybe convert particles into TER code only?
            inputs.forEach(input -> input.ifPresent(inputNode -> {
                BlockPos inputPos = ((NodeEnergyStorage)inputNode).getLocation();
                Vector3d spawn = Vector3d.atCenterOf(inputPos);
                Vector3d dest = Vector3d.atCenterOf(worldPosition);
                EnergyNodeParticleData data = new EnergyNodeParticleData(.2f, .5f, 1f);
                level.addParticle(data, spawn.x, spawn.y, spawn.z, dest.x, dest.y, dest.z);
            }));

            outputs.forEach(output -> output.ifPresent(outputNode -> {
                BlockPos outputPos = ((NodeEnergyStorage) outputNode).getLocation();
                Vector3d spawn = Vector3d.atCenterOf(worldPosition);
                Vector3d dest = Vector3d.atCenterOf(outputPos);
                EnergyNodeParticleData data = new EnergyNodeParticleData(1f, .5f, .1f);
                level.addParticle(data, spawn.x, spawn.y, spawn.z, dest.x, dest.y, dest.z);
            }));
        }
    }

    private void loadEnergyCapsFromLevel() {
        Set<BlockPos> invalid = new HashSet<>();
        for (BlockPos nodePos : connectedNodes) {
            // load and parse capability references
            // TODO - what happens if that chunk is not loaded? then inputs and outputs don't get filled!
            if (level != null && level.isLoaded(nodePos)) {
                final BlockState state = level.getBlockState(nodePos);
                final TileEntity tn = level.getBlockEntity(nodePos);
                if (tn instanceof EnergyNodeTile) {
                    final LazyOptional<IEnergyStorage> cap = tn.getCapability(CapabilityEnergy.ENERGY, null);
                    if (!cap.isPresent()) {
                        invalid.add(nodePos);
                        continue;
                    }

                    if(level.isClientSide)
                        renderBounds = getRenderBoundingBox().expandTowards(nodePos.getX()-this.worldPosition.getX(),
                                nodePos.getY()-this.worldPosition.getY(),
                                nodePos.getZ()-this.worldPosition.getZ());

                    switch (state.getValue(EnergyNodeBlock.PROP_INOUT)) {
                        case IN:
                            inputs.add(cap);
                            cap.addListener(removed -> {
                                this.inputs.remove(removed);
                                this.connectedNodes.remove(nodePos);
                                if(level.isClientSide)
                                    this.rebuildRenderBounds();
                            });
                            break;

                        case OUT:
                            outputs.add(cap);
                            cap.addListener(removed -> {
                                this.outputs.remove(removed);
                                this.connectedNodes.remove(nodePos);
                                if(level.isClientSide)
                                    this.rebuildRenderBounds();
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

    public void rebuildRenderBounds() {
        this.renderBounds = super.getRenderBoundingBox();
        for (BlockPos nodePos : connectedNodes) {
            AxisAlignedBB aabbNodePos = AxisAlignedBB.ofSize(1, 1, 1).move(Vector3d.atCenterOf(nodePos));
            renderBounds = getRenderBoundingBox().minmax(aabbNodePos);
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return renderBounds != null ? renderBounds : super.getRenderBoundingBox();
    }

    public void setTier(IControllerTier tier) {
        this.tier = tier;
        this.tierLO = LazyOptional.of(() -> tier);
    }
}
