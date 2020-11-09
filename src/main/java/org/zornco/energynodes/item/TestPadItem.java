package org.zornco.energynodes.item;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;

import javax.annotation.Nonnull;

public class TestPadItem extends Item {
    public TestPadItem() {
        super(new Item.Properties()
                .maxStackSize(1)
                .group(Registration.ITEM_GROUP));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(@Nonnull ItemUseContext context) {
        TileEntity tile = context.getWorld().getTileEntity(context.getPos());
        if (tile != null) {
            CompoundNBT nbt = new CompoundNBT();
            nbt = tile.write(nbt);
            EnergyNodes.LOGGER.info(nbt.toString());
            if (!context.getWorld().isRemote) {
                Utils.sendMessage(context.getPlayer(), nbt.toString());
            }
        }

        BlockState state = context.getWorld().getBlockState(context.getPos());
        if (state.getValues().size() > 0) {

            EnergyNodes.LOGGER.info(state.toString());
            if (!context.getWorld().isRemote) {
                Utils.sendMessage(context.getPlayer(), state.toString());
            }
        }

        return ActionResultType.func_233537_a_(context.getWorld().isRemote);
    }
}
