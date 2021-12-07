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
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.LINES,
                256, false, false,
                RenderType.CompositeState.builder()
                        .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(2.0)))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(NO_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .createCompositeState(false));
        public static final RenderType THICCCCC_LINES = RenderType.create("thicc_lines",
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.LINE_STRIP,
                256, false, false,
                RenderType.CompositeState.builder()
                        .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(5.0)))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(NO_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .createCompositeState(false));

        public LineTypes(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
            super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
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
        float f3 = 1.0f/(width + 15);
        matrixStack.scale(f3 * scale, -f3 * scale, f3);
        fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth(text,
                115), -width/2.0f + 0.5f, -4f, 0xffffff, false, matrixStack.last().pose(), buffer, false, 0, 140);

        matrixStack.scale(1/(f3 * scale), 1/(f3 * scale), f3);
        matrixStack.translate(-0.35F, -0.35F, 0.0F);
        matrixStack.scale(1/80F, 1/80F, f3);
        fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth(new TranslatableComponent(EnergyNodes.MOD_ID.concat(".ter.").concat(te.tier.getSerializedName())).getString(256),
                115), 0f, 0f, 0xffffff, false, matrixStack.last().pose(), buffer, false, 0, 140);
        matrixStack.popPose();


//        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof EnergyLinkerItem) {
//            matrixStack.pushPose();
//            Vec3 vector3d = Vec3.atCenterOf(te.getBlockPos());
//
//            final VertexConsumer lines = buffer.getBuffer(LineTypes.THICC_LINE);
//
//            matrixStack.translate(0.5F, 0.5, 0.5);
//
//            te.inputs.forEach(input -> input.ifPresent(inputNode -> {
//                Vec3 inputPos = Vec3.atCenterOf(((NodeEnergyStorage)inputNode).getLocation()).subtract(vector3d);
//                lines.vertex(matrixStack.last().pose(), 0,0,0).color(.2f, .5f, 1f, 0.5F).endVertex();
//                lines.vertex(matrixStack.last().pose(), (float) inputPos.x, (float) inputPos.y, (float) inputPos.z).color(.2f, .5f, 1f, 0.5F).endVertex();
//            }));
//            te.outputs.forEach(output -> output.ifPresent(outputNode -> {
//                Vec3 outputPos = Vec3.atCenterOf(((NodeEnergyStorage)outputNode).getLocation()).subtract(vector3d);
//                lines.vertex(matrixStack.last().pose(),0,0,0).color(1f, .5f, .1f, 0.5F).endVertex();
//                lines.vertex(matrixStack.last().pose(), (float) outputPos.x, (float) outputPos.y, (float) outputPos.z).color(1f, .5f, .1f, 0.5F).endVertex();
//            }));
//
//            //AxisAlignedBB bounds = te.getRenderBoundingBox().move(vector3d.reverse());
//            //WorldRenderer.renderLineBox(matrixStack, buffer.getBuffer(RenderType.lines()), bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ, 1F, 1F, 1F, 1F);
//            matrixStack.popPose();
//        }

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
