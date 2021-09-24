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
import java.util.Objects;

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
            if (!context.getLevel().isClientSide) {
                Utils.sendMessage(context.getPlayer(), nbt.toString());
            }
            if (Objects.requireNonNull(context.getPlayer()).isCrouching())
            {
                EnergyNodes.LOGGER.info((context.getLevel().isClientSide()?"CLIENT ":"SERVER ") + nbt.toString());
            }
        }

        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.getValues().size() > 0) {

            if (!context.getLevel().isClientSide) {
                Utils.sendMessage(context.getPlayer(), state.toString());
            }
        }

        return ActionResultType.sidedSuccess(context.getLevel().isClientSide);
    }
}
