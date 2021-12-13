package org.zornco.energynodes.compat.top;

import mcjty.theoneprobe.api.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.tile.EnergyControllerTile;

public class EnergyControllerProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(EnergyNodes.MOD_ID, "controller");
    }
    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, Player playerEntity, Level world, BlockState blockState, IProbeHitData iProbeHitData) {

        ILayoutStyle center = info.defaultLayoutStyle()
                .alignment(ElementAlignment.ALIGN_CENTER);
        IProbeInfo v = info.vertical(info.defaultLayoutStyle().spacing(-1));
        EnergyControllerTile tile = (EnergyControllerTile) world.getBlockEntity(iProbeHitData.getPos());
        if (tile != null) {
            v.horizontal(center)
                    .text(new TranslatableComponent(EnergyNodes.MOD_ID.concat(".top.transferred"), tile.transferredThisTick));
        }
    }
}