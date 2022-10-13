package org.zornco.energynodes.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.network.packets.PacketSyncControllerTier;
import org.zornco.energynodes.tiers.IControllerTier;
import org.zornco.energynodes.tile.BaseControllerTile;

import javax.annotation.Nonnull;

public class TierUpgradeItem extends Item {
    private final IControllerTier tier;

    public TierUpgradeItem(IControllerTier tier) {
        super(new Item.Properties()
                .tab(Registration.ITEM_GROUP));
        this.tier = tier;
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        if (world.isClientSide || player == null) {
            return InteractionResult.PASS;
        }
        BlockEntity tile = world.getBlockEntity(context.getClickedPos());
        if (tile == null) {
            return InteractionResult.FAIL;
        }
        LazyOptional<IControllerTier> capability = tile.getCapability(Registration.TIER_CAPABILITY);
        if (!capability.isPresent()) {
            return InteractionResult.FAIL;
        }
        if (!(tile instanceof BaseControllerTile controller)) {
            return InteractionResult.FAIL;
        }
        if (controller.getTier().getLevel() >= this.tier.getLevel()) {
            return InteractionResult.PASS;
        }
        controller.setTier(this.tier);
        capability.invalidate();

        NetworkManager.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(context.getClickedPos())), new PacketSyncControllerTier(controller));
        if (!player.isCreative()) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
