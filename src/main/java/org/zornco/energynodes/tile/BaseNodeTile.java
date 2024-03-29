package org.zornco.energynodes.tile;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.block.BaseNodeBlock;
import org.zornco.energynodes.graph.Node;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.network.packets.PacketRemoveNode;
import org.zornco.energynodes.network.packets.PacketSyncNodeData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashMap;


public abstract class BaseNodeTile extends BlockEntity implements INodeTile {
    //public final HashMap<Direction,BlockEntity> connectedTiles = new HashMap<>();

    @Nullable
    public IControllerTile controller;
    @Nullable
    public BlockPos controllerPos;

    public WeakReference<Node> nodeRef = null;

    public BaseNodeTile(BlockEntityType<?> ent, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(ent, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level != null) {
            if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof IControllerTile cont) {
                this.connectController(cont);
//                controller = cont;
//                final BaseNodeBlock.Flow flowDir = getBlockState().getValue(BaseNodeBlock.PROP_INOUT);
//                nodeRef = controller.getGraph().getNode(flowDir, this.worldPosition);
                //this.energyStorage.setController((EnergyControllerTile) controller);
                NetworkManager.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() ->
                    level.getChunkAt(controllerPos)), new PacketSyncNodeData(controllerPos, this.worldPosition));

            }
            if (controllerPos != null && nodeRef == null)
            {
                EnergyNodes.LOGGER.fatal("Node didn't get their ref set?!");
            }
            //getConnectedTiles();
//            EnergyControllerTile tile = energyStorage.getControllerTile();
//            if (tile != null && tile == controller) {
//
//                BaseNodeBlock.Flow flowDir = getBlockState().getValue(BaseNodeBlock.PROP_INOUT);
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
        else {
            EnergyNodes.LOGGER.warn("How is Level null??");
        }
    }

//    public HashMap<Direction, BlockEntity> getConnectedTiles() {
//        if (level != null) {
//            for (Direction ctDir : connectedTiles.keySet()) {
//                if (level.isLoaded(worldPosition.relative(ctDir))) {
//                    BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(ctDir));
//                    connectedTiles.replace(ctDir, blockEntity);
//                } else {
//                    connectedTiles.remove(ctDir);
//                }
//            }
//            for (Direction dir: Direction.values()) {
//                if (level.isLoaded(worldPosition.relative(dir)) && !connectedTiles.containsKey(dir)) {
//                    BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(dir));
//                    if (blockEntity != null && !(blockEntity instanceof BaseNodeTile))
//                        connectedTiles.put(dir, blockEntity);
//                }
//            }
//        }
//        return connectedTiles;
//    }


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
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    @Nullable
    @Override
    public IControllerTile getController() {
        // "lazily" try to get the controller BE if we know the pos
        if (controllerPos != null && controller == null)
        {
            if (level != null && level.getBlockEntity(controllerPos) instanceof IControllerTile cont) {
                controller = cont;
            } else {
                EnergyNodes.LOGGER.warn("Attempted to get Controller while level was null");
            }
        }
        return controller;
    }

    @Override
    public Capability<?> getCapabilityType() {
        return null;
    }

    @Override
    public WeakReference<Node> getNodeRef() {
        if (nodeRef == null || nodeRef.get() == null) {
            IControllerTile cont = getController();
            if (cont != null) {
                final BaseNodeBlock.Flow dir = getBlockState().getValue(BaseNodeBlock.PROP_INOUT);
                setNodeRef(new WeakReference<>(cont.getGraph().getNode(dir, this.worldPosition)));
            }
        }
        return nodeRef;
    }

    public void setNodeRef(WeakReference<Node> nodeRef) {
        this.nodeRef = nodeRef;
    }

    @Override
    public void clearConnection() {
        if (controllerPos != null && this.controller != null && level != null) {
            if (!level.isClientSide)
                NetworkManager.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
                    () -> level.getChunkAt(this.controllerPos)), new PacketRemoveNode(controller, getBlockPos()));
            this.controller.getGraph().removeNode(this.nodeRef.get());
            this.controller.rebuildRenderBounds();

        }
        this.nodeRef = null;
        this.controllerPos = null;
        this.controller = null;
    }

    @Override
    public void connectController(IControllerTile inController) {
        final BaseNodeBlock.Flow dir = getBlockState().getValue(BaseNodeBlock.PROP_INOUT);
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
        this.nodeRef = this.controller.getGraph().addNode(dir, this.worldPosition);
        this.controller.rebuildRenderBounds();
        if (getLevel() != null) {
            for (Direction facing : Direction.values()) {
                BlockPos neighbor = getBlockPos().relative(facing);
                BaseNodeBlock.connectToStorage(getLevel(), getBlockPos(), facing, neighbor);
            }
        }
    }

    public BaseNodeBlock.Flow getFlow() {
        return getBlockState().getValue(BaseNodeBlock.PROP_INOUT);
    }

    @Override
    public boolean canExtract(LazyOptional<?> adjacentStorageOptional) {
        return false;
    }

    @Override
    public boolean canReceive(LazyOptional<?> adjacentStorageOptional) {
        return true;
    }

}
