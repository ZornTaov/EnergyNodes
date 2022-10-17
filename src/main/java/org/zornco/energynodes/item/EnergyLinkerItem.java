package org.zornco.energynodes.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.tile.IControllerTile;
import org.zornco.energynodes.tile.INodeTile;
import org.zornco.energynodes.block.NodeBlockTags;
import org.zornco.energynodes.graph.ConnectionGraph;
import org.zornco.energynodes.graph.Node;

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
        ItemStack itemstack = context.getItemInHand();
        CompoundTag compoundTag = itemstack.getOrCreateTag();
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockState = level.getBlockState(blockpos);
        if (blockState.is(NodeBlockTags.NODE_TAG)) {
            compoundTag.put(EnergyNodeConstants.NBT_NODE_POS_KEY, NbtUtils.writeBlockPos(blockpos));
            Utils.SendSystemMessage(context, Component.translatable(
                EnergyNodes.MOD_ID.concat(".linker.start_connection"),
                Utils.getCoordinatesAsString(blockpos)));
            itemstack.setTag(compoundTag);
            return InteractionResult.SUCCESS;

        } else if (blockState.is(NodeBlockTags.CONTROLLER_TAG) && compoundTag.contains(
                EnergyNodeConstants.NBT_NODE_POS_KEY)) {
            if (!(level.getBlockEntity(blockpos) instanceof IControllerTile controllerTile)) {
                return InteractionResult.PASS;
            }
            BlockPos nodePos = NbtUtils.readBlockPos((CompoundTag) Objects.requireNonNull(
                compoundTag.get(EnergyNodeConstants.NBT_NODE_POS_KEY)));
            if (!(level.getBlockEntity(nodePos) instanceof INodeTile nodeTile)) {
                Utils.SendSystemMessage(context, Component.translatable(
                    EnergyNodes.MOD_ID.concat(".linker.node_missing"),
                    Utils.getCoordinatesAsString(nodePos)));
                compoundTag.remove(EnergyNodeConstants.NBT_NODE_POS_KEY);
                itemstack.setTag(compoundTag);
                return InteractionResult.PASS;
            }

            BlockState nodeState = level.getBlockState(nodePos);

            if (!((blockState.is(NodeBlockTags.ENERGY_TAG) && nodeState.is(NodeBlockTags.ENERGY_TAG)) ||
                (blockState.is(NodeBlockTags.FLUID_TAG) && nodeState.is(NodeBlockTags.FLUID_TAG))) )
                return InteractionResult.PASS;

            // check if node is out of range of controller
            if (blockpos.distManhattan(nodePos) >= controllerTile.getTier().getMaxRange()) {
                Utils.SendSystemMessage(context, Component.translatable(
                    EnergyNodes.MOD_ID.concat(".linker.node_out_of_range"),
                    controllerTile.getTier().getMaxRange()));
                return InteractionResult.PASS;
            }

            InteractionResult result = tryToLink(context, controllerTile, nodeTile);

            // on success, forget node position
            if (result == InteractionResult.SUCCESS) {
                if (level.isClientSide)
                    controllerTile.rebuildRenderBounds();
                compoundTag.remove(EnergyNodeConstants.NBT_NODE_POS_KEY);
                itemstack.setTag(compoundTag);
            }
            return result;
        } else {
            return super.useOn(context);
        }
    }

    private InteractionResult tryToLink(UseOnContext context, IControllerTile controller, INodeTile node) {
        ConnectionGraph graph = controller.getGraph();

        WeakReference<Node> nodeRef = node.getNodeRef();

        // node already linked, so disconnect it
        if (nodeRef != null && nodeRef.get() != null && controller.equals(node.getController())) {
            disconnectNode(node);
            Utils.SendSystemMessage(context, Component.translatable(EnergyNodes.MOD_ID.concat(".linker.disconnected"), Utils.getCoordinatesAsString(node.getBlockPos())));
            return InteractionResult.SUCCESS;
        }

        // new node, but controller has too many connections
        if(graph.getSize() >= controller.getTier().getMaxConnections()) {
            Utils.SendSystemMessage(context, Component.translatable(EnergyNodes.MOD_ID.concat(".linker.too_many_connections"), controller.getTier().getMaxConnections()));
            return InteractionResult.PASS;
        }

        // connect new node
        connectNode(controller, node);
        return InteractionResult.SUCCESS;
    }

    private void connectNode(IControllerTile controller, INodeTile node) {
//        if (node.getController() != null && !node.getController().getBlockPos().equals( controller.getBlockPos()))
//        {
//            //remove old connection, then rebuild old controller
//            disconnectNode(node);
//        }
        //make new connection
        node.connectController(controller);
    }

    private void disconnectNode(INodeTile node) {
        node.clearConnection();
    }
}
