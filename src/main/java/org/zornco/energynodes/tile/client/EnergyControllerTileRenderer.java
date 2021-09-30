package org.zornco.energynodes.tile.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.opengl.GL11;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.block.EnergyControllerBlock;
import org.zornco.energynodes.capability.NodeEnergyStorage;
import org.zornco.energynodes.item.EnergyLinkerItem;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.tile.EnergyControllerTile;

import javax.annotation.Nonnull;
import java.util.OptionalDouble;

public class EnergyControllerTileRenderer extends TileEntityRenderer<EnergyControllerTile> {
    public EnergyControllerTileRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    public static class LineTypes extends RenderType {
        public static final RenderType THICC_LINE = RenderType.create("thicc_line",
                DefaultVertexFormats.POSITION_COLOR,
                GL11.GL_LINES,
                256,
                RenderType.State.builder()
                        .setLineState(new RenderState.LineState(OptionalDouble.of(2.0)))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(NO_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .createCompositeState(false));
        public static final RenderType THICCCCC_LINES = RenderType.create("thicc_lines",
                DefaultVertexFormats.POSITION_COLOR,
                GL11.GL_LINE_LOOP,
                256,
                RenderType.State.builder()
                        .setLineState(new RenderState.LineState(OptionalDouble.of(5.0)))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(NO_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .createCompositeState(false));

        public LineTypes(String p_i225992_1_, VertexFormat p_i225992_2_, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_) {
            super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_, p_i225992_8_);
        }
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
        NetworkManager.RequestEnergyTransferred(te, 20);
        String text = te.transferredThisTick + "";
        int width = fontrenderer.width(text);
        float f3 = 1.0f/(width + 15);
        matrixStack.scale(f3 * scale, -f3 * scale, f3);
        fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth(text,
                115), -width/2.0f + 0.5f, -4f, 0xffffff, false, matrixStack.last().pose(), buffer, false, 0, 140);
        matrixStack.popPose();


        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof EnergyLinkerItem) {
            matrixStack.pushPose();
            Vector3d vector3d = Vector3d.atCenterOf(te.getBlockPos());

            final IVertexBuilder lines = buffer.getBuffer(LineTypes.THICC_LINE);

            //RenderSystem.pushMatrix();
            matrixStack.translate(0.5F, 0.5, 0.5);

            te.inputs.forEach(input -> input.ifPresent(inputNode -> {
                Vector3d inputPos = Vector3d.atCenterOf(((NodeEnergyStorage)inputNode).getLocation()).subtract(vector3d);
                lines.vertex(matrixStack.last().pose(), 0,0,0).color(.2f, .5f, 1f, 0.5F).endVertex();
                lines.vertex(matrixStack.last().pose(), (float) inputPos.x, (float) inputPos.y, (float) inputPos.z).color(.2f, .5f, 1f, 0.5F).endVertex();
            }));
            te.outputs.forEach(output -> output.ifPresent(outputNode -> {
                Vector3d outputPos = Vector3d.atCenterOf(((NodeEnergyStorage)outputNode).getLocation()).subtract(vector3d);
                lines.vertex(matrixStack.last().pose(),0,0,0).color(1f, .5f, .1f, 0.5F).endVertex();
                lines.vertex(matrixStack.last().pose(), (float) outputPos.x, (float) outputPos.y, (float) outputPos.z).color(1f, .5f, .1f, 0.5F).endVertex();
            }));

            //RenderSystem.popMatrix();
            matrixStack.popPose();
        }

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
