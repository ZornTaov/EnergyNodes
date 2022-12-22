package org.zornco.energynodes;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;

import java.util.OptionalDouble;

public class ClientUtils {

    public static void renderDebugText(BlockEntityRendererProvider.Context context, BlockPos p_114498_, String p_114499_, PoseStack matrixStack, MultiBufferSource buffer, int p_114502_) {
        double dist = context.getEntityRenderer().distanceToSqr(p_114498_.getX(), p_114498_.getY(), p_114498_.getZ());
        if (!(dist > 4096.0f)) {
            matrixStack.pushPose();
            matrixStack.translate(0.0D, 1.0, 0.0D);
            matrixStack.mulPose(context.getEntityRenderer().cameraOrientation());
            matrixStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = matrixStack.last().pose();
            float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            int j = (int)(f1 * 255.0F) << 24;

            Font font = Minecraft.getInstance().font;
            float width = (float)(-font.width(p_114499_) / 2);
            font.drawInBatch(p_114499_, width, 0, 0x20FFFFFF, false,
                matrix4f, buffer, false, j, p_114502_);
            font.drawInBatch(p_114499_, width, 0, 0xFFFFFFFF, false,
                matrix4f, buffer, false, 0, p_114502_);

            matrixStack.popPose();
        }
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
}
