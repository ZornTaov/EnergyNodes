package org.zornco.energynodes.tile;

import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.capability.NodeEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class EnergyNodeTile extends BlockEntity {
    public final NodeEnergyStorage energyStorage;
    private final LazyOptional<NodeEnergyStorage> energy;
    public final HashMap<Direction,BlockEntity> connectedTiles = new HashMap<>();

    @Nullable
    public BlockPos controllerPos;

    public EnergyNodeTile(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(Registration.ENERGY_TRANSFER_TILE.get(), pos, state);
        this.energyStorage = new NodeEnergyStorage(this);
        this.energy = LazyOptional.of(() -> this.energyStorage);
    }

    @Override
    public void onLoad() {
        if (level != null && !level.isClientSide)
        {
            MinecraftServer server = level.getServer();
            if (server != null) {
                server.tell(new TickTask(server.getTickCount() + 5, this::LoadConnectedTiles));
            }
        }
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        if (tag.get(EnergyNodeConstants.NBT_CONTROLLER_POS_KEY) != null)
            BlockPos.CODEC.decode(NbtOps.INSTANCE, tag.get(EnergyNodeConstants.NBT_CONTROLLER_POS_KEY))
                    .resultOrPartial(EnergyNodes.LOGGER::error)
                    .ifPresent(blockPosINBTPair -> this.controllerPos = blockPosINBTPair.getFirst());
        if (tag.get(EnergyNodeConstants.NBT_CONNECTED_TILES_KEY) != null)
            Utils.DIRECTION_LIST_CODEC.decode(NbtOps.INSTANCE, tag.getList(EnergyNodeConstants.NBT_CONNECTED_TILES_KEY, Tag.TAG_INT_ARRAY))
                    .resultOrPartial(EnergyNodes.LOGGER::error)
                    .ifPresent(listINBTPair -> listINBTPair.getFirst().forEach(direction -> connectedTiles.put(direction,
                            null)));
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        if (controllerPos != null)
            BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, controllerPos)
                    .resultOrPartial(EnergyNodes.LOGGER::error)
                    .ifPresent(inbt -> compound.put(EnergyNodeConstants.NBT_CONTROLLER_POS_KEY, inbt));
        if (getBlockState().getValue(EnergyNodeBlock.PROP_INOUT) == EnergyNodeBlock.Flow.OUT && connectedTiles.size() != 0)
            Utils.DIRECTION_LIST_CODEC.encodeStart(NbtOps.INSTANCE, new ArrayList<>(connectedTiles.keySet()))
                    .resultOrPartial(EnergyNodes.LOGGER::error)
                    .ifPresent(inbt -> compound.put(EnergyNodeConstants.NBT_CONNECTED_TILES_KEY, inbt));
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet){
        this.handleUpdateTag(packet.getTag());
        ModelDataManager.requestModelDataRefresh(this);
        Objects.requireNonNull(this.getLevel()).setBlocksDirty(this.worldPosition, this.getBlockState(), this.getBlockState());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY)
            return energy.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energy.invalidate();
    }

    private void LoadConnectedTiles() {
        if (level != null) {

            if (controllerPos != null && level.isLoaded(controllerPos.offset(worldPosition))) {
                this.energyStorage.setController((EnergyControllerTile) level.getBlockEntity(controllerPos.offset(worldPosition)));
            }
            for (Direction ctDir : connectedTiles.keySet()) {
                if (level.isLoaded(worldPosition.relative(ctDir))) {
                    BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(ctDir));
                    connectedTiles.replace(ctDir, blockEntity);
                } else {
                    connectedTiles.remove(ctDir);
                }
            }
            for (Direction dir: Direction.values()) {
                if (level.isLoaded(worldPosition.relative(dir)) && !connectedTiles.containsKey(dir)) {
                    BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(dir));
                    if (blockEntity != null)
                        connectedTiles.put(dir, blockEntity);
                }
            }
            if (energyStorage.getControllerTile() != null) {
                energy.addListener(removed -> {
                    energyStorage.getControllerTile().connectedNodes.remove(this.getBlockPos().subtract(controllerPos));

                    switch (getBlockState().getValue(EnergyNodeBlock.PROP_INOUT)) {
                        case IN -> energyStorage.getControllerTile().inputs.remove(removed);
                        case OUT -> energyStorage.getControllerTile().outputs.remove(removed);
                    }

                    energyStorage.getControllerTile().setChanged();
                });
            }
        }
    }
}
