package org.zornco.energynodes.tile;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.block.IControllerNode;
import org.zornco.energynodes.block.INodeTile;
import org.zornco.energynodes.capability.NodeEnergyStorage;
import org.zornco.energynodes.graph.Node;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.network.packets.PacketRemoveNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashMap;


public class EnergyNodeTile extends BlockEntity implements INodeTile {
    public final NodeEnergyStorage energyStorage;
    private final LazyOptional<NodeEnergyStorage> energy;
    public final HashMap<Direction,BlockEntity> connectedTiles = new HashMap<>();

    @Nullable
    public IControllerNode controller;
    @Nullable
    public BlockPos controllerPos;

    private WeakReference<Node> nodeRef = null;

    public EnergyNodeTile(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(Registration.ENERGY_TRANSFER_TILE.get(), pos, state);
        this.energyStorage = new NodeEnergyStorage(this);
        this.energy = LazyOptional.of(() -> this.energyStorage);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level != null) {
            if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof IControllerNode cont) {
                controller = cont;
                final EnergyNodeBlock.Flow flowDir = getBlockState().getValue(EnergyNodeBlock.PROP_INOUT);
                nodeRef = controller.getGraph().getNode(flowDir, this.worldPosition);
                this.energyStorage.setController((EnergyControllerTile) controller);
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
//            EnergyControllerTile tile = energyStorage.getControllerTile();
//            if (tile != null && tile == controller) {
//
//                EnergyNodeBlock.Flow flowDir = getBlockState().getValue(EnergyNodeBlock.PROP_INOUT);
//                var node = tile.getGraph().getNode(flowDir, this.worldPosition);
//
//                if (node.get() != null)
//                {
//                    setNodeRef(node);
//                }
//                else {
//                    switch (flowDir)
//                    {
//                        case OUT -> tile.getGraph().removeOutput(worldPosition);
//                        case IN -> tile.getGraph().removeInput(worldPosition);
//                    }
//                    clearConnection();
//                }
//            }
        }
    }


    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        if (tag.get(EnergyNodeConstants.NBT_CONTROLLER_POS_KEY) != null)
            BlockPos.CODEC.decode(NbtOps.INSTANCE, tag.get(EnergyNodeConstants.NBT_CONTROLLER_POS_KEY))
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(blockPosINBTPair -> this.controllerPos = Utils.getPosFromOffset(this.worldPosition,blockPosINBTPair.getFirst()));
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        if (controller != null)
            BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, Utils.getOffsetFromPos(controller.getBlockPos(), this.worldPosition))
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(inbt -> compound.put(EnergyNodeConstants.NBT_CONTROLLER_POS_KEY, inbt));
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }


    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY)
            return energy.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energy.invalidate();
    }

    @Override
    public IControllerNode getController() {
        return energyStorage.getControllerTile();
    }

    @Override
    public Capability<?> getCapabilityType() {
        return ForgeCapabilities.ENERGY;
    }

    @Override
    public WeakReference<Node> getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(WeakReference<Node> nodeRef) {
        this.nodeRef = nodeRef;
    }

    @Override
    public void clearConnection() {
        if (controllerPos != null && this.controller != null && level != null) {
            if (!level.isClientSide)
                NetworkManager.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.controllerPos)), new PacketRemoveNode(controller, getBlockPos()));
            this.controller.getGraph().removeNode(this.nodeRef.get());
            this.controller.rebuildRenderBounds();

        }
        this.nodeRef = null;
        this.controllerPos = null;
        this.controller = null;
        this.energyStorage.setController(null);
        this.energyStorage.setEnergyStored(0);
    }

    @Override
    public void connectController(IControllerNode inController) {
        final EnergyNodeBlock.Flow dir = getBlockState().getValue(EnergyNodeBlock.PROP_INOUT);
        if (this.controller != null && this.controller != inController)
        {
            // unlink

            if (this.nodeRef.get() != null) {
                var node = this.nodeRef.get();
                if (controllerPos != null && node != null && level != null) {
                    clearConnection();
                }
            }
        }

        this.controllerPos = inController.getBlockPos();
        this.controller = inController;
        this.energyStorage.setController((EnergyControllerTile) inController);
        this.nodeRef = this.controller.getGraph().addNode(dir, this.worldPosition);
        this.controller.rebuildRenderBounds();
    }
}
