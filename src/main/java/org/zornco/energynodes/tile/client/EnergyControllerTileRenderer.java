package org.zornco.energynodes.tile.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.block.BaseControllerBlock;
import org.zornco.energynodes.network.NetworkManager;
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
        FluidStack fluidStack = new FluidStack(Fluids.FLOWING_LAVA, 1000);
        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTex = fluidTypeExtensions.getStillTexture();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(stillTex);

        int color = 0x77FFFFFF;//fluidTypeExtensions.getTintColor(fluidStack);

        int[] col = new int[] {
            (color >> 16) & 0xFF,
            (color >> 8)  & 0xFF,
            color        & 0xFF,
            (color >> 24) & 0xFF
        };
        VertexConsumer builder = buffer.getBuffer(Sheets.translucentCullBlockSheet());
        matrixStack.translate(0.0F, 0.0, -0.01);

        Matrix4f matrix = matrixStack.last().pose();
        Matrix3f normal = matrixStack.last().normal();
        //EnergyNodes.LOGGER.info(i + ", " + i1);
        float minX = -0.5f;
        float maxX = 0.5f;
        float minY = 0.5f;
        float maxY = 1.5f;
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
