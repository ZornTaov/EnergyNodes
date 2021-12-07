package org.zornco.energynodes.particles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.zornco.energynodes.item.EnergyLinkerItem;

import javax.annotation.Nonnull;

import static org.zornco.energynodes.tile.client.EnergyControllerTileRenderer.LineTypes.THICCCCC_LINES;

public class EnergyNodeParticle extends TextureSheetParticle {
    private final Vec3 sourcePos;
    private final Vec3 targetPos;

    protected EnergyNodeParticle(ClientLevel world, double sourceX, double sourceY, double sourceZ, double targetX, double targetY, double targetZ, float red, float green, float blue) {
        super(world, sourceX, sourceY, sourceZ);
        sourcePos = new Vec3(sourceX, sourceY, sourceZ);
        targetPos = new Vec3(targetX, targetY, targetZ);
        rCol = red;
        gCol = green;
        bCol = blue;
        this.setGravity(0f);
        this.lifetime = 10 * (int)Math.round(sourcePos.distanceTo(targetPos)) + this.random.nextInt(12);
    }

    @Override
    public void render(@Nonnull VertexConsumer vert, @Nonnull Camera info, float tickDelta) {
        // TODO - Renders as black lines until inventory is opened?!
        //super.render(vert, info, tickDelta);
        PoseStack matrixStack = new PoseStack();
        Vec3 vector3d = info.getPosition();

        matrixStack.translate(-vector3d.x(), -vector3d.y(), -vector3d.z());

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        //VertexConsumer ivertexbuilder = bufferSource.getBuffer(THICCCCC_LINES);

        //renderOctahedron(matrixStack, ivertexbuilder);

        bufferSource.endBatch();
    }

    private void renderOctahedron(PoseStack matrixStack, VertexConsumer ivertexbuilder) {
        double circumscribedRadius = 0.1;
        Vec3[] octahedron = new Vec3[6];
        octahedron[0] = new Vec3(-circumscribedRadius, 0, 0);
        octahedron[1] = new Vec3(circumscribedRadius, 0, 0);
        octahedron[2] = new Vec3(0, -circumscribedRadius, 0);
        octahedron[3] = new Vec3(0, circumscribedRadius, 0);
        octahedron[4] = new Vec3(0, 0, -circumscribedRadius);
        octahedron[5] = new Vec3(0, 0, circumscribedRadius);
        int[] lineLoop1 = {2,4,3,5,2,1,3,0,5,1,4,0,2};
        for (int i: lineLoop1) {
            ivertexbuilder.vertex(matrixStack.last().pose(), (float)(x+octahedron[i].x), (float)(y+octahedron[i].y), (float)(z+octahedron[i].z)).color(rCol, gCol, bCol, 1F).endVertex();
        }
    }

    @Override
    public void tick() {
        //Just in case something goes weird, we remove the particle if it has been around too long.
        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        //prevPos is used in the render. if you don't do this your particle rubber bands (Like lag in an MMO).
        //This is used because ticks are 20 per second, and FPS is usually 60 or higher.
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        //Get the current position of the particle, and figure out the vector of where it's going
        Vec3 partPos = new Vec3(this.x, this.y, this.z);

        //The total distance between the particle and target
        double totalDistance = targetPos.distanceTo(partPos);
        if (totalDistance < 0.1 || !(Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof EnergyLinkerItem))
            this.remove();

        double speedAdjust = 1;
        Vec3 lerped = lerp(sourcePos, targetPos, this.age/(double)this.lifetime);
        xd = lerped.x() - this.x;// (targetX - this.x) / speedAdjust;
        yd = lerped.y() - this.y;// (targetY - this.y) / speedAdjust;
        zd = lerped.z() - this.z;// (targetZ - this.z) / speedAdjust;

        //Perform the ACTUAL move of the particle.
        this.move(this.xd, this.yd, this.zd);

    }

    public Vec3 lerp(Vec3 posA, Vec3 posB, double amount) {
        double f = 1.0-amount;
        double d0 = posA.x * f + posB.x * amount;
        double d1 = posA.y * f + posB.y * amount;
        double d2 = posA.z * f + posB.z * amount;
        return new Vec3(d0, d1, d2);
    }

    @Override
    public void move(double mx, double my, double mz) {
        this.setBoundingBox(this.getBoundingBox().move(mx, my, mz));
        this.setLocationFromBoundingbox();
    }

    public void setGravity(float value) {
        gravity = value;
    }

    @Nonnull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public static class FACTORY implements ParticleProvider<EnergyNodeParticleData> {

        public FACTORY(SpriteSet sprites) { }

        @Override
        public Particle createParticle(EnergyNodeParticleData data, @Nonnull ClientLevel world, double sourceX, double sourceY, double sourceZ, double targetX, double targetY, double targetZ) {
            return new EnergyNodeParticle(world, sourceX, sourceY, sourceZ, targetX, targetY, targetZ, data.r, data.g, data.b);
        }
    }
}
