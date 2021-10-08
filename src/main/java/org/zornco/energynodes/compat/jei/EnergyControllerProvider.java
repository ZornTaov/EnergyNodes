package org.zornco.energynodes.compat.jei;

import mcjty.theoneprobe.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.tile.EnergyControllerTile;

public class EnergyControllerProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return EnergyNodes.MOD_ID + "_controller";
    }
    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData iProbeHitData) {

        ILayoutStyle center = info.defaultLayoutStyle()
                .alignment(ElementAlignment.ALIGN_CENTER);
        IProbeInfo v = info.vertical(info.defaultLayoutStyle().spacing(-1));
        EnergyControllerTile tile = (EnergyControllerTile) world.getBlockEntity(iProbeHitData.getPos());
        if (tile != null) {
            v.horizontal(center)
                    .text(new TranslationTextComponent(EnergyNodes.MOD_ID.concat(".top.transferred"), tile.transferredThisTick));
        }
    }
}
