package org.zornco.energynodes.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
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
import java.util.Objects;

public class EnergyLinkerItem extends Item {
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
        TileEntity tile = world.getTileEntity(blockpos);
        CompoundNBT compoundnbt = itemstack.hasTag() ? itemstack.getTag() : new CompoundNBT();
        if (compoundnbt != null) {
            if (world.getBlockState(blockpos).getBlock() instanceof EnergyControllerBlock && compoundnbt.contains("TransferPos")) {

                if (tile != null) {
                    BlockPos otherPos = NBTUtil.readBlockPos((CompoundNBT) Objects.requireNonNull(compoundnbt.get("TransferPos")));
                    TileEntity tile2 = world.getTileEntity(otherPos);
                    if (tile2 != null) {
                        if (!((EnergyControllerTile) tile).connectedNodes.contains(otherPos)) {
                            ((EnergyControllerTile) tile).connectedNodes.add(otherPos);
                            ((EnergyNodeTile) tile2).controllerPos = blockpos;
                            if (!context.getWorld().isRemote) {
                                Utils.sendSystemMessage(context.getPlayer(), "Connected to: " + compoundnbt.get("TransferPos"));
                            }
                        } else {
                            ((EnergyControllerTile) tile).connectedNodes.remove(otherPos);
                            ((EnergyNodeTile) tile2).controllerPos = blockpos;
                            if (!context.getWorld().isRemote) {
                                Utils.sendSystemMessage(context.getPlayer(), "Disconnected to: " + compoundnbt.get("TransferPos"));
                            }
                        }
                    } else {
                        if (((EnergyControllerTile) tile).connectedNodes.contains(otherPos)) {
                            ((EnergyControllerTile) tile).connectedNodes.remove(otherPos);
                            if (!context.getWorld().isRemote) {
                                Utils.sendSystemMessage(context.getPlayer(), "Node missing, removed: " + compoundnbt.get("TransferPos"));
                            }
                        } else {
                            if (!context.getWorld().isRemote) {
                                Utils.sendSystemMessage(context.getPlayer(), "Node missing at: " + compoundnbt.get("TransferPos"));
                            }
                        }
                    }
                } else {
                    if (!context.getWorld().isRemote) {
                        Utils.sendSystemMessage(context.getPlayer(), "Controller has no Tile?!");
                    }
                }
                compoundnbt.remove("TransferPos");
                itemstack.setTag(compoundnbt);
            } else if ((world.getBlockState(blockpos).getBlock() instanceof EnergyNodeBlock)) {
                compoundnbt.put("TransferPos", NBTUtil.writeBlockPos(blockpos));
                //EnergyNodes.LOGGER.info("Connected from: "+compoundnbt.get("TransferPos"));
                if (!context.getWorld().isRemote) {
                    Utils.sendSystemMessage(context.getPlayer(), "Starting connection from: " + compoundnbt.get("TransferPos"));
                }
                itemstack.setTag(compoundnbt);
            } else {
                return super.onItemUse(context);
            }
        }
        return ActionResultType.PASS;
    }
}
