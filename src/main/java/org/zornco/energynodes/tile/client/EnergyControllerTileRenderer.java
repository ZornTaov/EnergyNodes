package org.zornco.energynodes.tile.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.block.EnergyControllerBlock;
import org.zornco.energynodes.tile.EnergyControllerTile;

import javax.annotation.Nonnull;

public class EnergyControllerTileRenderer extends TileEntityRenderer<EnergyControllerTile> {
    public EnergyControllerTileRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(EnergyControllerTile te, float v, MatrixStack matrixStack, @Nonnull IRenderTypeBuffer buffer, int i, int i1) {
        matrixStack.pushPose();
        int scale = 1;
        matrixStack.translate(0.5F, 0.5, 0.5);
        Direction orientation = te.getBlockState().getValue(EnergyControllerBlock.PROP_FACING);
        matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), -getHudAngle(orientation), true));

        matrixStack.translate(0.0F, 0.0, 0.5325);

        FontRenderer fontrenderer = Minecraft.getInstance().font;
        String text = te.transferredThisTick + "000000000";
        int width = fontrenderer.width(text);
        float f3 = 1.0f/(width + 15);
        matrixStack.scale(f3 * scale, -f3 * scale, f3);
        fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth(text,
                115), -width/2.0f + 0.5f, -4f, 0xffffff, false, matrixStack.last().pose(), buffer, false, 0, 140);
        matrixStack.popPose();
    }
    private static float getHudAngle(Direction orientation) {
        float f3 = 0.0f;

        if (orientation != null) {
            switch (orientation) {
                case NORTH:
                    f3 = 180.0F;
                    break;
                case WEST:
                    f3 = 90.0F;
                    break;
                case EAST:
                    f3 = -90.0F;
                    break;
                default:
                    f3 = 0.0f;
            }
        }
        return f3;
    }
    public static void register()
    {
        ClientRegistry.bindTileEntityRenderer(Registration.ENERGY_CONTROLLER_TILE.get(), EnergyControllerTileRenderer::new);
    }
}
