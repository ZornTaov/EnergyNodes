package org.zornco.energynodes.tile.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.zornco.energynodes.ClientUtils;
import org.zornco.energynodes.graph.CapabilityNode;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.tile.controllers.FluidControllerTile;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class FluidControllerTileRenderer extends BaseControllerTileRenderer<FluidControllerTile> {
    public FluidControllerTileRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    @Nonnull
    public String getText(FluidControllerTile te) {
        NetworkManager.RequestTransferred(te, 20);
        return te.transferredThisTick + "";
//        return super.getText(te);
    }

    @Override
    public void renderAdditional(FluidControllerTile te, PoseStack matrixStack, MultiBufferSource buffer, Font fontrenderer, int overlay, int light) {

        //render fluid
        FluidStack fluidStack = te.fluidStack;
        if (fluidStack != null) {
            matrixStack.pushPose();
            IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
            ResourceLocation stillTex = fluidTypeExtensions.getStillTexture();
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTex);

            int color = fluidTypeExtensions.getTintColor(fluidStack);

            int[] col = new int[]{
                (color >> 16) & 0xFF,
                (color >> 8) & 0xFF,
                color & 0xFF,
                (color >> 24) & 0xFF
            };
//          RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidStack.getFluid().defaultFluidState());
//          VertexConsumer builder = buffer.getBuffer(renderType);
            VertexConsumer builder = buffer.getBuffer(Sheets.translucentCullBlockSheet());

            matrixStack.translate(0.0F, 0.0, -0.01);

            Matrix4f matrix = matrixStack.last().pose();
            Matrix3f normal = matrixStack.last().normal();
            //EnergyNodes.LOGGER.info(i + ", " + i1);
            float minX = -0.1f;
            float maxX =  0.1f;
            float minY = -0.37f;
            float maxY = -0.17f;
            float ny = 1;

            float minU = sprite.getU(0);
            float maxU = sprite.getU(16);
            float minV = sprite.getV(0);
            float maxV = sprite.getV(16);

            builder.vertex(matrix, minX, minY, 0).color(col[0], col[1], col[2], col[3]).uv(maxU, minV).overlayCoords(overlay).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0, ny, 0).endVertex();
            builder.vertex(matrix, maxX, minY, 0).color(col[0], col[1], col[2], col[3]).uv(minU, minV).overlayCoords(overlay).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0, ny, 0).endVertex();
            builder.vertex(matrix, maxX, maxY, 0).color(col[0], col[1], col[2], col[3]).uv(minU, maxV).overlayCoords(overlay).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0, ny, 0).endVertex();
            builder.vertex(matrix, minX, maxY, 0).color(col[0], col[1], col[2], col[3]).uv(maxU, maxV).overlayCoords(overlay).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0, ny, 0).endVertex();
            matrixStack.popPose();
        }

        //render text
        matrixStack.pushPose();
        matrixStack.translate(0.10F, -0.25F, 0.0F);
        matrixStack.scale(1 / 80F, -1 / 80F, 1);
        fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth("mB/t", 115),
            0f, 0f, 0xffffff, false,
            matrixStack.last().pose(), buffer, false, 0, 140);
        matrixStack.popPose();

    }

    @Override
    protected void renderDebugInfo(FluidControllerTile te, PoseStack matrixStack, MultiBufferSource buffer) {
        matrixStack.pushPose();
        matrixStack.translate(0.5F, 0.5, 0.5);

        //I hate this
        Predicate<IFluidHandler> storagePredicate = storage ->
            (storage.isFluidValid(0, te.fluidStack) && storage.getFluidInTank(0).getAmount() != storage.getTankCapacity(0));

        Set<IFluidHandler> optionals = te.getGraph().getAllOutputs(storagePredicate);
//            .stream()
//            .map(CapabilityNode::cap)
//            .map(LazyOptional::<IFluidHandler>cast)
//            .map(lo -> lo.orElseThrow(
//                () -> new RuntimeException("Failed to get present adjacent storage for controller " + te.getBlockPos())))
//            .filter(storagePredicate).toList();

        long filteredOutputCount = optionals.size();
        ClientUtils.renderDebugText(context, te.getBlockPos(), filteredOutputCount + "", matrixStack, buffer, 140);

        matrixStack.popPose();
    }
}
