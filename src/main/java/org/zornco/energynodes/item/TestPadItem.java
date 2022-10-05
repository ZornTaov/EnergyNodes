package org.zornco.energynodes.item;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.block.EnergyControllerBlock;
import org.zornco.energynodes.block.IControllerNode;

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
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
        if (tile != null) {

            if (tile instanceof IControllerNode node)
            {

                EnergyNodes.LOGGER.info((context.getLevel().isClientSide()?"CLIENT ":"SERVER ") + node.getGraph().getNodeGraph().toString());
            }
            CompoundTag nbt = tile.saveWithFullMetadata();
            if (!context.getLevel().isClientSide) {
                Utils.sendMessage(context.getPlayer(), wrapComment(nbt.toString(), 50));
            }
            if (Objects.requireNonNull(context.getPlayer()).isCrouching())
            {
                EnergyNodes.LOGGER.info((context.getLevel().isClientSide()?"CLIENT ":"SERVER ") + nbt);
            }
        }

        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.getValues().size() > 0) {

            if (!context.getLevel().isClientSide) {
                //Utils.sendMessage(context.getPlayer(), state.toString());
            }
        }

        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
    private String wrapComment(String comment, int length) {

        if(comment.length() <= length) return comment;

        StringBuilder stringBuilder = new StringBuilder();

        int spaceIndex = -1;

        for(int i = 0; i < comment.length(); i ++) {

            if(i % length == 0 && spaceIndex > -1) {
                stringBuilder.replace(spaceIndex+1, spaceIndex+1, "\n");
                spaceIndex = -1;
                i++;
            }

            if(comment.charAt(i) == ',') {
                spaceIndex = i;
            }

            stringBuilder.append(comment.charAt(i));
        }

        return stringBuilder.toString();
    }
}
