package org.zornco.energynodes.tile.old;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.RegistryObject;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.block.old.EnergyNodeBlockOLD;
import org.zornco.energynodes.capability.old.NodeEnergyStorageOLD;
import org.zornco.energynodes.item.EnergyLinkerItem;
import org.zornco.energynodes.nbt.NbtListCollector;
import org.zornco.energynodes.particles.EnergyNodeParticleData;
import org.zornco.energynodes.tiers.ControllerTier;
import org.zornco.energynodes.tiers.IControllerTier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class EnergyControllerTileOLD extends BlockEntity {

    protected int ticks = 0;
    protected long totalEnergyTransferred = 0;
    protected long totalEnergyTransferredLastTick = 0;

    public final HashSet<BlockPos> connectedNodes = new HashSet<>();
    public final HashSet<LazyOptional<IEnergyStorage>> inputs = new HashSet<>();
    public final HashSet<LazyOptional<IEnergyStorage>> outputs = new HashSet<>();

    public long transferredThisTick;
    private AABB renderBounds;

    public IControllerTier tier;
    private LazyOptional<IControllerTier> tierLO;

    public EnergyControllerTileOLD(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(Registration.ENERGY_CONTROLLER_TILE.get(), pos, state);
        this.tier = new ControllerTier();
        tierLO = LazyOptional.of(() -> this.tier);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        connectedNodes.forEach(nodePos -> {
            if(level != null) {
                final BlockEntity tile = level.getBlockEntity(getNodeFromController(nodePos));
                if (tile instanceof EnergyNodeTileOLD enTile) {
                    enTile.controllerPos = null;
                    enTile.energyStorage.setController(null);
                    enTile.energyStorage.setEnergyStored(0);
                    enTile.setChanged();
                }
            }
        });

        EnergyNodes.LOGGER.debug("Controller Removed");
    }

    @Override
    public void invalidateCaps() {
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
                    server.tell(new TickTask(server.getTickCount(), this::loadEnergyCapsFromLevel));
                }
            }
            else
            {
                renderBounds = super.getRenderBoundingBox();
            }
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        // inputPositions
        BlockPos.CODEC.listOf().fieldOf(EnergyNodeConstants.NBT_CONNECTED_INPUT_NODES_KEY).codec()
                .parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(connectedNodes::addAll);

        // outputs
        BlockPos.CODEC.listOf().fieldOf(EnergyNodeConstants.NBT_CONNECTED_OUTPUT_NODES_KEY).codec()
                .parse(NbtOps.INSTANCE, tag)
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
        EnergyNodes.LOGGER.debug("LOAD CN:"+connectedNodes.size());
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        checkConnections();
        compound.put(EnergyNodeConstants.NBT_CONNECTED_INPUT_NODES_KEY, getStorageNbt(inputs));
        compound.put(EnergyNodeConstants.NBT_CONNECTED_OUTPUT_NODES_KEY, getStorageNbt(outputs));

        compound.putLong(EnergyNodeConstants.NBT_TOTAL_ENERGY_TRANSFERRED_KEY, this.totalEnergyTransferred);
        compound.putLong(EnergyNodeConstants.NBT_TRANSFERRED_THIS_TICK_KEY, this.transferredThisTick);
        compound.putString(EnergyNodeConstants.NBT_TIER, this.tier.getSerializedName());
        EnergyNodes.LOGGER.debug("SAVEADDITIONAL I:"+inputs.size()+" O:"+outputs.size());
    }

    @Nonnull
    private ListTag getStorageNbt(Collection<LazyOptional<IEnergyStorage>> storages) {
        return storages.stream()
                .map(LazyOptional::resolve)
                .filter(Optional::isPresent)
                .map(opt -> opt.map(storage -> {
                    if (storage instanceof NodeEnergyStorageOLD) {
                        BlockPos pos = ((NodeEnergyStorageOLD) storage).getLocation().subtract(worldPosition);
                        return BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).getOrThrow(false, EnergyNodes.LOGGER::error);
                    }

                    return null;
                }).orElse(null))
                .filter(Objects::nonNull)
                .collect(NbtListCollector.toNbtList());
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

//    @Override
//    public void handleUpdateTag(CompoundTag tag) {
//        load(tag);
//    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

//    @Override
//    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
//        if (this.getLevel() != null) {
//            this.handleUpdateTag(packet.getTag());
//            ModelDataManager.requestModelDataRefresh(this);
//            this.getLevel().setBlocksDirty(this.worldPosition, this.getBlockState(), this.getBlockState());
//        }
//    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == Registration.TIER_CAPABILITY)
            return tierLO.cast();

        return super.getCapability(cap, side);
    }

    public boolean canReceiveEnergy(EnergyNodeTileOLD nodeTile) {
        return this.connectedNodes.contains(nodeTile.getBlockPos().subtract(worldPosition));
    }

    public int receiveEnergy(EnergyNodeTileOLD inputTile, int maxReceive, boolean simulate) {
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
                EnergyNodeTileOLD node = ((NodeEnergyStorageOLD) storage).getNodeTile();
                return node.connectedTiles.entrySet()
                        // TODO: Make another cap for other Lazy storage
                        .stream()
                        .filter(entry -> entry.getValue() != null)
                        .mapToInt(tile -> tile.getValue()
                                .getCapability(ForgeCapabilities.ENERGY,
                                    tile.getKey().getOpposite())
                                .map(iEnergyStorage -> (iEnergyStorage.canReceive() ||
                                        iEnergyStorage.getEnergyStored() / iEnergyStorage.getMaxEnergyStored() != 1) ? 1 : 0)
                                .orElse(0))
                        .sum();
            }).orElse(0)).sum();
        } else {
            connectedEnergyTilesAmount = 1;
        }

        for (BlockPos nodePos : this.connectedNodes) {
            EnergyNodeTileOLD outputTile = (EnergyNodeTileOLD) level.getBlockEntity(getNodeFromController(nodePos));
            if (outputTile != null) {
                int transferredThisTile = 0;
                for (Map.Entry<Direction, BlockEntity> tileEntry : outputTile.connectedTiles.entrySet()) {
                    Direction facing = tileEntry.getKey();
                    //BlockPos outputOffset =outputEntry.relative(facing);
                    BlockEntity otherTile = tileEntry.getValue();
                    int amountReceivedThisBlock = 0;
                    if (otherTile != null && !(otherTile instanceof EnergyNodeTileOLD)) {
                        LazyOptional<IEnergyStorage> adjacentStorageOptional = otherTile.getCapability(ForgeCapabilities.ENERGY, facing.getOpposite());
                        if (adjacentStorageOptional.isPresent()) {
                            IEnergyStorage adjacentStorage = adjacentStorageOptional.orElseThrow(
                                () -> new RuntimeException("Failed to get present adjacent storage for pos " + this.worldPosition));
                            int amountToSend = (int) ((this.tier.getMaxTransfer() == EnergyNodeConstants.UNLIMITED_RATE ?
                                maxReceive / connectedEnergyTilesAmount :
                                Math.min(maxReceive, this.tier.getMaxTransfer())) / connectedEnergyTilesAmount);
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

    static public void tick(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull BlockEntity tile) {
        EnergyControllerTileOLD ECTile = (EnergyControllerTileOLD) tile;
        if (!level.isClientSide) {
            // Compute the FE transfer in this tick by taking the difference between total transfer this
            // tick and the total transfer last tick
            ECTile.transferredThisTick = Math.abs(ECTile.totalEnergyTransferred);

            ECTile.inputs.forEach(opt -> opt.ifPresent(storage -> {
                final NodeEnergyStorageOLD s = (NodeEnergyStorageOLD) storage;
                s.setEnergyStored(0);
            }));

            ECTile.outputs.forEach(opt -> opt.ifPresent(storage -> {
                final NodeEnergyStorageOLD s = (NodeEnergyStorageOLD) storage;
                s.setEnergyStored(0);
            }));

            ECTile.totalEnergyTransferred = 0;
            if (ECTile.transferredThisTick > 0) {
                ECTile.setChanged();
            }

            if (ECTile.ticks % 10 == 0) {
                ECTile.setChanged();
            }
        } else {
            if (ECTile.ticks % 10 == 0) {

                ECTile.checkConnections();
                ECTile.spawnParticles();
            }
        }
        ECTile.ticks = ++ECTile.ticks % 20;
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
                BlockPos inputPos = ((NodeEnergyStorageOLD)inputNode).getLocation();
                Vec3 spawn = Vec3.atCenterOf(inputPos);
                Vec3 dest = Vec3.atCenterOf(worldPosition);
                EnergyNodeParticleData data = new EnergyNodeParticleData(.2f, .5f, 1f);
                level.addParticle(data, spawn.x, spawn.y, spawn.z, dest.x, dest.y, dest.z);
            }));

            outputs.forEach(output -> output.ifPresent(outputNode -> {
                BlockPos outputPos = ((NodeEnergyStorageOLD) outputNode).getLocation();
                Vec3 spawn = Vec3.atCenterOf(worldPosition);
                Vec3 dest = Vec3.atCenterOf(outputPos);
                EnergyNodeParticleData data = new EnergyNodeParticleData(1f, .5f, .1f);
                level.addParticle(data, spawn.x, spawn.y, spawn.z, dest.x, dest.y, dest.z);
            }));
        }
    }

    private void loadEnergyCapsFromLevel() {
        Set<BlockPos> invalid = new HashSet<>();
        for (BlockPos nodePos1 : connectedNodes) {
            // load and parse capability references
            // TODO - what happens if that chunk is not loaded? then inputs and outputs don't get filled!
            BlockPos nodePos = getNodeFromController(nodePos1);
            if (level != null && level.isLoaded(nodePos)) {
                final BlockState state = level.getBlockState(nodePos);
                final BlockEntity tn = level.getBlockEntity(nodePos);
                if (tn instanceof EnergyNodeTileOLD) {
                    final LazyOptional<IEnergyStorage> cap = tn.getCapability(ForgeCapabilities.ENERGY, null);
                    if (!cap.isPresent()) {
                        invalid.add(nodePos);
                        continue;
                    }

                    if(level.isClientSide)
                        renderBounds = getRenderBoundingBox().expandTowards(nodePos.getX()-this.worldPosition.getX(),
                                nodePos.getY()-this.worldPosition.getY(),
                                nodePos.getZ()-this.worldPosition.getZ());

                    switch (state.getValue(EnergyNodeBlockOLD.PROP_INOUT)) {
                        case IN -> {
                            inputs.add(cap);
                            EnergyNodes.LOGGER.debug("Input Added I:"+inputs.size());
                            cap.addListener(removed -> {
                                this.inputs.remove(removed);
                                EnergyNodes.LOGGER.debug("Input Removed I:"+inputs.size());
                                this.connectedNodes.remove(nodePos);
                                if (level.isClientSide)
                                    this.rebuildRenderBounds();
                            });
                        }
                        case OUT -> {
                            outputs.add(cap);
                            EnergyNodes.LOGGER.debug("Output Added O:"+outputs.size());
                            cap.addListener(removed -> {
                                this.outputs.remove(removed);
                                EnergyNodes.LOGGER.debug("Output Removed O:"+outputs.size());
                                this.connectedNodes.remove(nodePos);
                                if (level.isClientSide)
                                    this.rebuildRenderBounds();
                            });
                        }
                        default -> {
                        }
                    }
                }
            }
        }

        connectedNodes.removeAll(invalid);
        EnergyNodes.LOGGER.debug("connectedNodes Changed CN:"+connectedNodes.size());

    }

    public void rebuildRenderBounds() {
        this.renderBounds = super.getRenderBoundingBox();
        for (BlockPos nodePos : connectedNodes) {
            AABB aabbNodePos = AABB.ofSize(Vec3.atCenterOf(getNodeFromController(nodePos)), 1, 1, 1);
            renderBounds = getRenderBoundingBox().minmax(aabbNodePos);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return renderBounds != null ? renderBounds : super.getRenderBoundingBox();
    }

    public void setTier(IControllerTier tier) {
        this.tier = tier;
        this.tierLO = LazyOptional.of(() -> tier);
    }
    private BlockPos getNodeFromController(BlockPos nodePos)
    {
        return worldPosition.offset(nodePos);
    }
}
