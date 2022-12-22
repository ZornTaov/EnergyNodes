package org.zornco.energynodes.tile.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.ClientUtils;
import org.zornco.energynodes.graph.CapabilityNode;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.tile.controllers.EnergyControllerTile;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class EnergyControllerTileRenderer extends BaseControllerTileRenderer<EnergyControllerTile> {
    public EnergyControllerTileRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    @Nonnull
    public String getText(EnergyControllerTile te) {
        NetworkManager.RequestTransferred(te, 20);
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

    @Override
    protected void renderDebugInfo(EnergyControllerTile te, PoseStack matrixStack, MultiBufferSource buffer) {
        matrixStack.pushPose();
        matrixStack.translate(0.5F, 0.5, 0.5);

        //I REALLY hate this
        Predicate<IEnergyStorage> storagePredicate = storage ->
            (storage.canReceive() && storage.getEnergyStored() != storage.getMaxEnergyStored());
        Set<IEnergyStorage> optionals = te.getGraph().getAllOutputs(storagePredicate);
//            .stream()
//            .map(CapabilityNode::cap)
//            .map(LazyOptional::<IEnergyStorage>cast)
//            .map(LazyOptional::resolve)
//            .filter(Optional::isPresent)
//            .map(Optional::get)
//            .filter(storagePredicate).toList();
        long filteredOutputCount = optionals.size();
        ClientUtils.renderDebugText(context, te.getBlockPos(), filteredOutputCount + "", matrixStack, buffer, 140);

        matrixStack.popPose();

    }
}
