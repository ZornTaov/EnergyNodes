package org.zornco.energynodes.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.block.NodeBlockTags;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.block.IControllerNode;
import org.zornco.energynodes.graph.Node;
import org.zornco.energynodes.tile.EnergyNodeTile;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.*;

public class EnergyLinkerItem extends Item {

    public EnergyLinkerItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .tab(Registration.ITEM_GROUP));
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        BlockPos blockpos = context.getClickedPos();
        Level world = context.getLevel();
        ItemStack itemstack = context.getItemInHand();
        BlockState blockState = world.getBlockState(blockpos);
        CompoundTag compoundnbt = itemstack.hasTag() ? itemstack.getTag() : new CompoundTag();
        if (compoundnbt != null) {
            if (blockState.is(NodeBlockTags.NODE_TAG)) {
                compoundnbt.put(EnergyNodeConstants.NBT_NODE_POS_KEY, NbtUtils.writeBlockPos(blockpos));
                Utils.SendSystemMessage(context, Component.translatable(EnergyNodes.MOD_ID.concat(".linker.start_connection"), Utils.getCoordinatesAsString(blockpos)));
                itemstack.setTag(compoundnbt);
                return InteractionResult.SUCCESS;

            } else if (blockState.is(NodeBlockTags.CONTROLLER_TAG) && compoundnbt.contains(EnergyNodeConstants.NBT_NODE_POS_KEY)) {
                if(world.getBlockEntity(blockpos) instanceof IControllerNode controllerTile) {
                    BlockPos nodePos = NbtUtils.readBlockPos((CompoundTag) Objects.requireNonNull(compoundnbt.get(EnergyNodeConstants.NBT_NODE_POS_KEY)));
                    EnergyNodeTile nodeTile = (EnergyNodeTile) world.getBlockEntity(nodePos);
                    if (nodeTile == null) {
                        Utils.SendSystemMessage(context, Component.translatable(EnergyNodes.MOD_ID.concat(".linker.node_missing"), Utils.getCoordinatesAsString(nodePos)));
                        return InteractionResult.PASS;
                    }
                    if (blockpos.distManhattan(nodePos) >= controllerTile.getTier().getMaxRange()) {
                        Utils.SendSystemMessage(context, Component.translatable(EnergyNodes.MOD_ID.concat(".linker.node_out_of_range"), controllerTile.getTier().getMaxRange()));
                        return InteractionResult.PASS;
                    }

                    InteractionResult result = updateControllerPosList(context,
                        controllerTile,
                        nodeTile);
                    if (result == InteractionResult.SUCCESS) {
                        if (world.isClientSide)
                            controllerTile.rebuildRenderBounds();
                        compoundnbt.remove(EnergyNodeConstants.NBT_NODE_POS_KEY);
                        itemstack.setTag(compoundnbt);
                    }
                    return result;
                }
            } else {
                return super.useOn(context);
            }
        }
        return InteractionResult.PASS;
    }

    // TODO - Split and transform into link/unlink.
    private static InteractionResult updateControllerPosList(@Nonnull UseOnContext context, IControllerNode controller, EnergyNodeTile nodeTile) {
        final EnergyNodeBlock.Flow dir = nodeTile.getBlockState().getValue(EnergyNodeBlock.PROP_INOUT);
        Direction hit = context.getClickedFace();

        BlockPos nodeFromController = nodeTile.getBlockPos().subtract(controller.getBlockPos());
        WeakReference<Node> nodeRef = nodeTile.getNodeRef();
        if (nodeRef != null) {
            // unlink
            Node node = nodeRef.get();
            if(node == null) return InteractionResult.PASS;
            switch (dir) {
                case IN -> controller.getGraph().removeInput(node.pos());
                case OUT -> controller.getGraph().removeOutput(node.pos());
            }
            nodeTile.clearConnection();
            Utils.SendSystemMessage(context,Component.translatable(EnergyNodes.MOD_ID.concat(".linker.disconnected"), Utils.getCoordinatesAsString(nodeFromController)));
        } else {
            // Link
            if (controller.getGraph().getSize() >= controller.getTier().getMaxConnections()) {
                Utils.SendSystemMessage(context, Component.translatable(EnergyNodes.MOD_ID.concat(".linker.too_many_connections"), controller.getTier().getMaxConnections()));
                return InteractionResult.PASS;
            }
            var node = switch (dir) {
                case IN -> controller.getGraph().addInput(nodeFromController);
                case OUT -> controller.getGraph().addOutput(nodeFromController);
            };
            nodeTile.setNodeRef(node);

//            LazyOptional<?> storage = nodeTile.getCapability(nodeTile.getCapabilityType(), Direction.DOWN);
//            storage.addListener(removed -> {
//
//                switch (dir) {
//                    case IN -> controller.getGraph().removeInput(nodeFromController);
//                    case OUT -> controller.getGraph().removeOutput(nodeFromController);
//                }
//
//                controller.setChanged();
//            });

            nodeTile.connectController(controller);
            Utils.SendSystemMessage(context, Component.translatable(EnergyNodes.MOD_ID.concat(".linker.connected_to"), Utils.getCoordinatesAsString(nodeFromController)));
        }
        return InteractionResult.SUCCESS;
    }

}
