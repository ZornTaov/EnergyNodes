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
import net.minecraftforge.network.PacketDistributor;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.block.INodeTile;
import org.zornco.energynodes.block.NodeBlockTags;
import org.zornco.energynodes.block.IControllerNode;
import org.zornco.energynodes.graph.ConnectionGraph;
import org.zornco.energynodes.graph.Node;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.network.packets.PacketRemoveNode;

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
            if(level.getBlockEntity(blockpos) instanceof IControllerNode controllerTile) {
                BlockPos nodePos = NbtUtils.readBlockPos((CompoundTag) Objects.requireNonNull(
                    compoundTag.get(EnergyNodeConstants.NBT_NODE_POS_KEY)));
                INodeTile nodeTile = (INodeTile) level.getBlockEntity(nodePos);

                // check if node block is missing
                if (nodeTile == null) {
                    Utils.SendSystemMessage(context, Component.translatable(
                        EnergyNodes.MOD_ID.concat(".linker.node_missing"),
                        Utils.getCoordinatesAsString(nodePos)));
                    compoundTag.remove(EnergyNodeConstants.NBT_NODE_POS_KEY);
                    itemstack.setTag(compoundTag);
                    return InteractionResult.PASS;
                }

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
            }
        } else {
            return super.useOn(context);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult tryToLink(UseOnContext context, IControllerNode controller, INodeTile node) {
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

    private void connectNode(IControllerNode controller, INodeTile node) {
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
