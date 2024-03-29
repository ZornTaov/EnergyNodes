package org.zornco.energynodes.tile.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.zornco.energynodes.ClientUtils;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.block.BaseControllerBlock;
import org.zornco.energynodes.graph.Node;
import org.zornco.energynodes.item.EnergyLinkerItem;
import org.zornco.energynodes.tile.BaseControllerTile;
import org.zornco.energynodes.tile.BaseNodeTile;
import org.zornco.energynodes.tile.controllers.EnergyControllerTile;
import org.zornco.energynodes.tile.controllers.FluidControllerTile;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BaseControllerTileRenderer<ControllerTile extends BaseControllerTile> implements BlockEntityRenderer<ControllerTile> {
    protected final BlockEntityRendererProvider.Context context;

    public BaseControllerTileRenderer(BlockEntityRendererProvider.Context ctx) {

        context = ctx;
    }
    @Override
    public void render(@Nonnull ControllerTile te, float v, @Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int i, int i1) {

        renderScreen(te, matrixStack, buffer, i, i1);
        //renderDebugInfo(te, matrixStack, buffer);


        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof EnergyLinkerItem) {
            matrixStack.pushPose();
            matrixStack.translate(0.5F, 0.5, 0.5);

            final VertexConsumer lines = buffer.getBuffer(ClientUtils.LineTypes.THICC_LINE);


            for (Node inputNode : te.getGraph().getInputNodes()) {
                Vec3 inputPos = Vec3.atLowerCornerOf(inputNode.pos().subtract(te.getBlockPos()));

                float f = (float) inputPos.x;
                float f1 = (float) inputPos.y;
                float f2 = (float) inputPos.z;
                float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
                f /= f3;
                f1 /= f3;
                f2 /= f3;
                lines.vertex(matrixStack.last().pose(), 0, 0, 0).color(.2f, .5f, 1f, 0.5F).normal(matrixStack.last().normal(), f, f1, f2).endVertex();
                lines.vertex(matrixStack.last().pose(), (float) inputPos.x, (float) inputPos.y, (float) inputPos.z).color(.2f, .5f, 1f, 0.5F).normal(matrixStack.last().normal(), f, f1, f2).endVertex();
            }
            for (Node outputNode : te.getGraph().getOutputNodes()) {
                Vec3 outputPos = Vec3.atLowerCornerOf(outputNode.pos().subtract(te.getBlockPos()));

                float f = (float) outputPos.x;
                float f1 = (float) outputPos.y;
                float f2 = (float) outputPos.z;
                float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
                f /= f3;
                f1 /= f3;
                f2 /= f3;
                lines.vertex(matrixStack.last().pose(), 0, 0, 0).color(1f, .5f, .1f, 0.5F).normal(matrixStack.last().normal(), f, f1, f2).endVertex();
                lines.vertex(matrixStack.last().pose(), (float) outputPos.x, (float) outputPos.y, (float) outputPos.z).color(1f, .5f, .1f, 0.5F).normal(matrixStack.last().normal(), f, f1, f2).endVertex();
            }
            if(Minecraft.getInstance().player.isCrouching()) {
                Vec3 vector3d = Vec3.atCenterOf(te.getBlockPos());
                AABB bounds = te.getRenderBoundingBox().move(vector3d.reverse());
                LevelRenderer.renderLineBox(matrixStack, buffer.getBuffer(RenderType.lines()), bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ, 1F, 1F, 1F, 1F);
            }
            matrixStack.popPose();
        }

    }

    protected void renderDebugInfo(ControllerTile te, PoseStack matrixStack, MultiBufferSource buffer) {
    }

    private void renderScreen(ControllerTile te, PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int overlay, int light) {
        matrixStack.pushPose();
        matrixStack.translate(0.5F, 0.5, 0.5);

        Direction orientation = te.getBlockState().getValue(BaseControllerBlock.PROP_FACING);
        matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), -getHudAngle(orientation), true));

        matrixStack.translate(0.0F, 0.0, 0.5325);

        Font fontrenderer = Minecraft.getInstance().font;
        renderTier(te, matrixStack, buffer, fontrenderer);
        renderText(te, matrixStack, buffer, fontrenderer);
        renderAdditional(te, matrixStack, buffer, fontrenderer, overlay, light);

        matrixStack.popPose();
    }

    public void renderAdditional(ControllerTile te, PoseStack matrixStack, MultiBufferSource buffer, Font fontrenderer, int overlay, int light) {
    }

    public void renderTier(ControllerTile te, PoseStack matrixStack, @Nonnull MultiBufferSource buffer, Font fontrenderer) {
        matrixStack.pushPose();
        matrixStack.translate(-0.35F, 0.35F, 0.0F);
        matrixStack.scale(1 / 80F, -1 / 80F, 1);
        fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth(
            Component.translatable(EnergyNodes.MOD_ID.concat(".ter.")
                .concat(te.getTier().getSerializedName())).getString(256),
            115), 0f, 0f, 0xffffff, false,
            matrixStack.last().pose(), buffer, false, 0, 140);
        matrixStack.popPose();
    }

    public void renderText(ControllerTile te, PoseStack matrixStack, @Nonnull MultiBufferSource buffer, Font fontrenderer) {
        int scale = 1;
        String text = getText(te);
        int width = fontrenderer.width(text);
        float textWidth = 1.0f / (width + 15);
        matrixStack.pushPose();
        matrixStack.scale(textWidth * scale, -textWidth * scale, 1);
        fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth(text, 115),
            -width / 2.0f + 0.5f, -4f, 0xffffff, false,
            matrixStack.last().pose(), buffer, false, 0, 140);
        matrixStack.popPose();
    }

    @Nonnull
    public String getText(ControllerTile te) {
        return te.ticks + "";
    }

    protected static float getHudAngle(Direction orientation) {
        float f3 = 0.0f;

        if (orientation != null) {
            f3 = switch (orientation) {
                case NORTH -> 180.0F;
                case WEST -> 90.0F;
                case EAST -> -90.0F;
                default -> 0.0f;
            };
        }
        return f3;
    }

    @Override
    public boolean shouldRenderOffScreen(@Nonnull ControllerTile te) {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof EnergyLinkerItem;
    }
}
