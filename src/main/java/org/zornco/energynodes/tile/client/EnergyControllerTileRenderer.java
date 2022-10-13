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
