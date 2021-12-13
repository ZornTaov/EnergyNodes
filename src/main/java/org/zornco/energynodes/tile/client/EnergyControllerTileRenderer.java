package org.zornco.energynodes.tile.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import com.mojang.math.Quaternion;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Vector3f;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.ClientRegistry;
import org.lwjgl.opengl.GL11;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.block.EnergyControllerBlock;
import org.zornco.energynodes.capability.NodeEnergyStorage;
import org.zornco.energynodes.item.EnergyLinkerItem;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.tile.EnergyControllerTile;

import javax.annotation.Nonnull;
import java.util.OptionalDouble;

public class EnergyControllerTileRenderer implements BlockEntityRenderer<EnergyControllerTile> {
    public EnergyControllerTileRenderer(BlockEntityRendererProvider.Context ctx) {

    }

    public static class LineTypes extends RenderType {
        public static final RenderType THICC_LINE = RenderType.create("thicc_line",
                DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.LINES,
                256, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_LINES_SHADER)
                        .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(4.0)))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(NO_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false));
        public static final RenderType THICCCCC_LINES = RenderType.create("thiccccc_lines",
                DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.LINE_STRIP,
                256, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_LINES_SHADER)
                        .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(8.0)))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(NO_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setCullState(NO_CULL)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .createCompositeState(false));

        public LineTypes(String s, VertexFormat vf, VertexFormat.Mode m, int i, boolean b1, boolean b2, Runnable r1, Runnable r2) {
            super(s, vf, m, i, b1, b2, r1, r2);
            throw new IllegalStateException("This class is not meant to be constructed!");
        }
    }
    @Override
    public void render(EnergyControllerTile te, float v, PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int i, int i1) {
        matrixStack.pushPose();
        int scale = 1;
        matrixStack.translate(0.5F, 0.5, 0.5);
        Direction orientation = te.getBlockState().getValue(EnergyControllerBlock.PROP_FACING);
        matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), -getHudAngle(orientation), true));

        matrixStack.translate(0.0F, 0.0, 0.5325);

        Font fontrenderer = Minecraft.getInstance().font;
        NetworkManager.RequestEnergyTransferred(te, 20);
        String text = te.transferredThisTick + "";
        int width = fontrenderer.width(text);
        float textWidth = 1.0f/(width + 15);
        matrixStack.scale(textWidth * scale, -textWidth * scale, textWidth);
        fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth(text,
                115), -width/2.0f + 0.5f, -4f, 0xffffff, false, matrixStack.last().pose(), buffer, false, 0, 140);

        matrixStack.scale(1/(textWidth * scale), 1/(textWidth * scale), textWidth);
        matrixStack.translate(-0.35F, -0.35F, 0.0F);
        matrixStack.scale(1/80F, 1/80F, textWidth);
        fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth(new TranslatableComponent(EnergyNodes.MOD_ID.concat(".ter.").concat(te.tier.getSerializedName())).getString(256),
                115), 0f, 0f, 0xffffff, false, matrixStack.last().pose(), buffer, false, 0, 140);
        matrixStack.popPose();


        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof EnergyLinkerItem) {
            matrixStack.pushPose();
            Vec3 vector3d = Vec3.atCenterOf(te.getBlockPos());

            final VertexConsumer lines = buffer.getBuffer(LineTypes.THICC_LINE);

            matrixStack.translate(0.5F, 0.5, 0.5);

            te.inputs.forEach(input -> input.ifPresent(inputNode -> {
                Vec3 inputPos = Vec3.atCenterOf(((NodeEnergyStorage)inputNode).getLocation()).subtract(vector3d);

                float f = (float)inputPos.x;
                float f1 = (float)inputPos.y;
                float f2 = (float)inputPos.z;
                float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
                f /= f3;
                f1 /= f3;
                f2 /= f3;
                lines.vertex(matrixStack.last().pose(), 0,0,0).color(.2f, .5f, 1f, 0.5F).normal(matrixStack.last().normal(), f, f1, f2).endVertex();
                lines.vertex(matrixStack.last().pose(), (float) inputPos.x, (float) inputPos.y, (float) inputPos.z).color(.2f, .5f, 1f, 0.5F).normal(matrixStack.last().normal(), f, f1, f2).endVertex();
            }));
            te.outputs.forEach(output -> output.ifPresent(outputNode -> {
                Vec3 outputPos = Vec3.atCenterOf(((NodeEnergyStorage)outputNode).getLocation()).subtract(vector3d);

                float f = (float)outputPos.x;
                float f1 = (float)outputPos.y;
                float f2 = (float)outputPos.z;
                float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
                f /= f3;
                f1 /= f3;
                f2 /= f3;
                lines.vertex(matrixStack.last().pose(),0,0,0).color(1f, .5f, .1f, 0.5F).normal(matrixStack.last().normal(), f, f1, f2).endVertex();
                lines.vertex(matrixStack.last().pose(), (float) outputPos.x, (float) outputPos.y, (float) outputPos.z).color(1f, .5f, .1f, 0.5F).normal(matrixStack.last().normal(), f, f1, f2).endVertex();
            }));

            //AxisAlignedBB bounds = te.getRenderBoundingBox().move(vector3d.reverse());
            //WorldRenderer.renderLineBox(matrixStack, buffer.getBuffer(RenderType.lines()), bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ, 1F, 1F, 1F, 1F);
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

    @Override
    public boolean shouldRenderOffScreen(@Nonnull EnergyControllerTile te) {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof EnergyLinkerItem;
    }

}
