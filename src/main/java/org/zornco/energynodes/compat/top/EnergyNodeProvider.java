package org.zornco.energynodes.compat.top;

import mcjty.theoneprobe.api.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.tile.BaseNodeTile;

public class EnergyNodeProvider implements IProbeInfoProvider {
    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(EnergyNodes.MOD_ID, "_node");
    }
    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, Player playerEntity, Level world, BlockState blockState, IProbeHitData iProbeHitData) {

        ILayoutStyle center = info.defaultLayoutStyle()
                .alignment(ElementAlignment.ALIGN_CENTER);
        IProbeInfo v = info.vertical(info.defaultLayoutStyle().spacing(-1));
        if(world.getBlockEntity(iProbeHitData.getPos()) instanceof BaseNodeTile tile) {
            if (tile.controllerPos != null) {
                v.horizontal(center)
                    .text(Component.translatable(EnergyNodes.MOD_ID.concat(".top.connected_to"), Utils.getCoordinatesAsString(tile.controllerPos)));
            /*if (blockState.get(PROP_INOUT) == Flow.OUT )
                v.horizontal(center)
                        .text(new TranslationTextComponent(EnergyNodes.MOD_ID.concat(".connected_to")))
                        .text(new StringTextComponent(tile.connectedTiles.size() + ""));*/
            }
        }
    }
}
