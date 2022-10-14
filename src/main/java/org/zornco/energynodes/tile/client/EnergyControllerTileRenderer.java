package org.zornco.energynodes.tile.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.zornco.energynodes.tile.EnergyControllerTile;

import javax.annotation.Nonnull;

public class EnergyControllerTileRenderer extends BaseControllerTileRenderer<EnergyControllerTile> {
    public EnergyControllerTileRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    @Nonnull
    public String getText(EnergyControllerTile te) {
//        NetworkManager.RequestEnergyTransferred(te, 20);
//        return te.transferredThisTick + "";
        return super.getText(te);
    }

    @Override
    public void renderAdditional(EnergyControllerTile te, PoseStack matrixStack, MultiBufferSource buffer, Font fontrenderer, int overlay, int light) {

        //render fluid
        matrixStack.pushPose();
        FluidStack fluidStack = new FluidStack(Fluids.WATER, 1000);
        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTex = fluidTypeExtensions.getStillTexture();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTex);

        int color = 0xFFFFFFFF;//fluidTypeExtensions.getTintColor(fluidStack);

        int[] col = new int[] {
            (color >> 16) & 0xFF,
            (color >> 8)  & 0xFF,
            color        & 0xFF,
            (color >> 24) & 0xFF
        };
        RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidStack.getFluid().defaultFluidState());
        VertexConsumer builder = buffer.getBuffer(renderType);
        matrixStack.translate(0.0F, 0.0, -0.01);

        Matrix4f matrix = matrixStack.last().pose();
        Matrix3f normal = matrixStack.last().normal();
        //EnergyNodes.LOGGER.info(i + ", " + i1);
        float minX = -0.3f;
        float maxX = 0.3f;
        float minY = 0.7f;
        float maxY = 1.3f;
        float ny = 1;

        float minU = sprite.getU(0);
        float maxU = sprite.getU(16);
        float minV = sprite.getV(0);
        float maxV = sprite.getV(16);

        builder.vertex(matrix, minX, minY, 0).color(col[0], col[1], col[2], col[3]).uv(maxU, minV).overlayCoords(overlay).uv2(light).normal(normal, 0, ny, 0).endVertex();
        builder.vertex(matrix, maxX, minY, 0).color(col[0], col[1], col[2], col[3]).uv(minU, minV).overlayCoords(overlay).uv2(light).normal(normal, 0, ny, 0).endVertex();
        builder.vertex(matrix, maxX, maxY, 0).color(col[0], col[1], col[2], col[3]).uv(minU, maxV).overlayCoords(overlay).uv2(light).normal(normal, 0, ny, 0).endVertex();
        builder.vertex(matrix, minX, maxY, 0).color(col[0], col[1], col[2], col[3]).uv(maxU, maxV).overlayCoords(overlay).uv2(light).normal(normal, 0, ny, 0).endVertex();

        matrixStack.popPose();
//        RenderSystem.enableBlend();
//        matrixStack.pushPose();
//        {
//            FluidStack fluidStack = new FluidStack(Fluids.WATER, 1000);
//            matrixStack.translate(-0.25F, 0.25, -0.01);
//            matrixStack.scale(1 / 16F, -1 / 16F, 1/16F);
//            drawFluid(matrixStack, 8,8, fluidStack);
//        }
//        matrixStack.popPose();
//        RenderSystem.setShaderColor(1, 1, 1, 1);
//        RenderSystem.disableBlend();
//
        //render text
        matrixStack.pushPose();
        matrixStack.translate(0.10F, -0.25F, 0.0F);
        matrixStack.scale(1 / 80F, -1 / 80F, 1);
        fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth("RF/t", 115),
            0f, 0f, 0xffffff, false,
            matrixStack.last().pose(), buffer, false, 0, 140);
        matrixStack.popPose();

    }
    private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of fluid are still visible
    private static final int TEXTURE_SIZE = 16;

    private void drawFluid(PoseStack poseStack, final int width, final int height, FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        if (fluid.isSame(Fluids.EMPTY)) {
            return;
        }

        TextureAtlasSprite fluidStillSprite = getStillFluidSprite(fluidStack);
        int fluidColor = getColorTint(fluidStack);

        long amount = fluidStack.getAmount();
        long scaledAmount = (amount * height) / 1000;

        if (amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
            scaledAmount = MIN_FLUID_HEIGHT;
        }
        if (scaledAmount > height) {
            scaledAmount = height;
        }

        drawTiledSprite(poseStack, width, height, fluidColor, scaledAmount, fluidStillSprite);
    }

    private TextureAtlasSprite getStillFluidSprite(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation fluidStill = renderProperties.getStillTexture(fluidStack);

        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
    }

    private int getColorTint(FluidStack ingredient) {
        Fluid fluid = ingredient.getFluid();
        IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);
        return renderProperties.getTintColor(ingredient);
    }

    private static void drawTiledSprite(PoseStack poseStack, final int tiledWidth, final int tiledHeight, int color, long scaledAmount, TextureAtlasSprite sprite) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        Matrix4f matrix = poseStack.last().pose();
        setGLColorFromInt(color);

        final int xTileCount = tiledWidth / TEXTURE_SIZE;
        final int xRemainder = tiledWidth - (xTileCount * TEXTURE_SIZE);
        final long yTileCount = scaledAmount / TEXTURE_SIZE;
        final long yRemainder = scaledAmount - (yTileCount * TEXTURE_SIZE);

        final int yStart = tiledHeight;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int width = (xTile == xTileCount) ? xRemainder : TEXTURE_SIZE;
                long height = (yTile == yTileCount) ? yRemainder : TEXTURE_SIZE;
                int x = (xTile * TEXTURE_SIZE);
                int y = yStart - ((yTile + 1) * TEXTURE_SIZE);
                if (width > 0 && height > 0) {
                    long maskTop = TEXTURE_SIZE - height;
                    int maskRight = TEXTURE_SIZE - width;

                    drawTextureWithMasking(matrix, x, y, sprite, maskTop, maskRight, 0);
                }
            }
        }
    }

    private static void setGLColorFromInt(int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float alpha = ((color >> 24) & 0xFF) / 255F;

        RenderSystem.setShaderColor(red, green, blue, alpha);
    }
    private static void drawTextureWithMasking(Matrix4f matrix, float xCoord, float yCoord, TextureAtlasSprite textureSprite, long maskTop, long maskRight, float zLevel) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMax = uMax - (maskRight / 16F * (uMax - uMin));
        vMax = vMax - (maskTop / 16F * (vMax - vMin));

        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, xCoord, yCoord + 16, zLevel).uv(uMin, vMax).endVertex();
        bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + 16, zLevel).uv(uMax, vMax).endVertex();
        bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).uv(uMax, vMin).endVertex();
        bufferBuilder.vertex(matrix, xCoord, yCoord + maskTop, zLevel).uv(uMin, vMin).endVertex();
        tessellator.end();
    }
}
