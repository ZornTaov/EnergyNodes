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
                .stacksTo(1)
                .tab(Registration.ITEM_GROUP));
    }

    @Nonnull
    @Override
    public ActionResultType useOn(@Nonnull ItemUseContext context) {
        TileEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
        if (tile != null) {
            CompoundNBT nbt = new CompoundNBT();
            nbt = tile.save(nbt);
            EnergyNodes.LOGGER.info(nbt.toString());
            if (!context.getLevel().isClientSide) {
                Utils.sendMessage(context.getPlayer(), nbt.toString());
            }
        }

        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.getValues().size() > 0) {

            EnergyNodes.LOGGER.info(state.toString());
            if (!context.getLevel().isClientSide) {
                Utils.sendMessage(context.getPlayer(), state.toString());
            }
        }

        return ActionResultType.sidedSuccess(context.getLevel().isClientSide);
    }
}
