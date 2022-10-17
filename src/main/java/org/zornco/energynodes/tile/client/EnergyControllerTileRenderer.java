package org.zornco.energynodes.tile.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.tile.controllers.EnergyControllerTile;

import javax.annotation.Nonnull;

public class EnergyControllerTileRenderer extends BaseControllerTileRenderer<EnergyControllerTile> {
    public EnergyControllerTileRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    @Nonnull
    public String getText(EnergyControllerTile te) {
        NetworkManager.RequestEnergyTransferred(te, 20);
        return te.transferredThisTick + "";
//        return super.getText(te);
    }

    @Override
    public void renderAdditional(EnergyControllerTile te, PoseStack matrixStack, MultiBufferSource buffer, Font fontrenderer, int overlay, int light) {

        //render text
        matrixStack.pushPose();
        matrixStack.translate(0.10F, -0.25F, 0.0F);
        matrixStack.scale(1 / 80F, -1 / 80F, 1);
        fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth("RF/t", 115),
            0f, 0f, 0xffffff, false,
            matrixStack.last().pose(), buffer, false, 0, 140);
        matrixStack.popPose();

    }
}
