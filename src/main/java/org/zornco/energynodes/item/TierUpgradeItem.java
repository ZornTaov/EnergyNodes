package org.zornco.energynodes.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.network.packets.PacketEnergyTransferredResponse;
import org.zornco.energynodes.network.packets.PacketSyncController;
import org.zornco.energynodes.tiers.IControllerTier;
import org.zornco.energynodes.tile.EnergyControllerTile;

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
    public ActionResultType useOn(@Nonnull ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getLevel();
        if (world.isClientSide || player == null) {
            return ActionResultType.PASS;
        }
        TileEntity tile = world.getBlockEntity(context.getClickedPos());
        if (tile == null) {
            return ActionResultType.FAIL;
        }
        LazyOptional<IControllerTier> capability = tile.getCapability(Registration.TIER_CAPABILITY);
        if (!capability.isPresent()) {
            return ActionResultType.FAIL;
        }
        if (!(tile instanceof EnergyControllerTile)) {
            return ActionResultType.FAIL;
        }
        EnergyControllerTile controller = (EnergyControllerTile) tile;
        if (controller.tier.getLevel() >= this.tier.getLevel()) {
            return ActionResultType.PASS;
        }
        controller.setTier(this.tier);
        capability.invalidate();

        NetworkManager.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(context.getClickedPos())), new PacketSyncController(controller));
        if (!player.isCreative()) {
            context.getItemInHand().shrink(1);
        }
        return ActionResultType.SUCCESS;
    }
}
