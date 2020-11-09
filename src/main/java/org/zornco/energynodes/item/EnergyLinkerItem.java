package org.zornco.energynodes.item;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.block.EnergyControllerBlock;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.tile.EnergyControllerTile;
import org.zornco.energynodes.tile.EnergyNodeTile;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class EnergyLinkerItem extends Item {
    private static final String NBT_NODE_POS_KEY = "node-pos";

    public EnergyLinkerItem() {
        super(new Item.Properties()
                .maxStackSize(1)
                .group(Registration.ITEM_GROUP));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(@Nonnull ItemUseContext context) {
        BlockPos blockpos = context.getPos();
        World world = context.getWorld();
        ItemStack itemstack = context.getItem();
        BlockState blockState = world.getBlockState(blockpos);
        CompoundNBT compoundnbt = itemstack.hasTag() ? itemstack.getTag() : new CompoundNBT();
        if (compoundnbt != null) {
            if (blockState.getBlock() instanceof EnergyControllerBlock && compoundnbt.contains(NBT_NODE_POS_KEY)) {

                EnergyControllerTile tile1 = (EnergyControllerTile) world.getTileEntity(blockpos);
                if (tile1 != null) {
                    BlockPos otherPos = NBTUtil.readBlockPos((CompoundNBT) Objects.requireNonNull(compoundnbt.get(NBT_NODE_POS_KEY)));
                    BlockState blockState1 = world.getBlockState(otherPos);
                    EnergyNodeTile tile2 = (EnergyNodeTile)world.getTileEntity(otherPos);
                    if (tile2 != null) {
                        updateControllerPosList(context,
                                blockpos,
                                otherPos,
                                blockState1.get(EnergyNodeBlock.PROP_INOUT) ? tile1.connectedOutputNodes : tile1.connectedInputNodes,
                                tile1,
                                tile2);
                    } else {
                        //tile1.connectedInputNodes.remove(otherPos);
                        SendSystemMessage(context, "Node missing at: " + otherPos.getCoordinatesAsString());
                        return ActionResultType.PASS;
                    }
                    compoundnbt.remove(NBT_NODE_POS_KEY);
                    itemstack.setTag(compoundnbt);
                    return ActionResultType.SUCCESS;
                } else {
                    SendSystemMessage(context, "Controller has no Tile?!");
                    return ActionResultType.PASS;
                }
            } else if ((blockState.getBlock() instanceof EnergyNodeBlock)) {
                compoundnbt.put(NBT_NODE_POS_KEY, NBTUtil.writeBlockPos(blockpos));
                SendSystemMessage(context, "Starting connection from: " + blockpos.getCoordinatesAsString());
                itemstack.setTag(compoundnbt);
                return ActionResultType.SUCCESS;
            } else {
                return super.onItemUse(context);
            }
        }
        return ActionResultType.PASS;
    }

    private void updateControllerPosList(@Nonnull ItemUseContext context,
                                         BlockPos blockpos,
                                         BlockPos otherPos,
                                         List<BlockPos> list,
                                         EnergyControllerTile tile1,
                                         EnergyNodeTile nodeTile) {
        if (list.contains(otherPos)) {
            list.remove(otherPos);
            nodeTile.controllerPos = null;
            nodeTile.energyStorage.setController(null);
            SendSystemMessage(context, "Disconnected to: " + otherPos.getCoordinatesAsString());
        } else {
            list.add(otherPos);
            nodeTile.controllerPos = blockpos;
            nodeTile.energyStorage.setController(tile1);
            SendSystemMessage(context, "Connected to: " + otherPos.getCoordinatesAsString());
        }
    }

    private void SendSystemMessage(@Nonnull ItemUseContext context, String s) {
        if (!context.getWorld().isRemote) {
            Utils.sendSystemMessage(context.getPlayer(), s);
        }
    }
}
