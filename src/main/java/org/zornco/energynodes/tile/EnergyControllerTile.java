package org.zornco.energynodes.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.RegistryObject;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.graph.ConnectionGraph;
import org.zornco.energynodes.graph.Node;
import org.zornco.energynodes.item.EnergyLinkerItem;
import org.zornco.energynodes.particles.EnergyNodeParticleData;
import org.zornco.energynodes.tiers.ControllerTier;
import org.zornco.energynodes.tiers.IControllerTier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EnergyControllerTile extends BlockEntity implements IControllerTile {

    protected int ticks = 0;
    protected long totalEnergyTransferred = 0;
    protected long totalEnergyTransferredLastTick = 0;

    public long transferredThisTick;
    private AABB renderBounds;

    private IControllerTier tier;
    private LazyOptional<IControllerTier> tierLO;
    private final ConnectionGraph graph;

    public EnergyControllerTile(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(Registration.ENERGY_CONTROLLER_TILE.get(), pos, state);

        if(!state.is(Registration.ENERGY_CONTROLLER_BLOCK.get()))
            EnergyNodes.LOGGER.fatal("Invalid Controller created!");
        this.tier = new ControllerTier();
        tierLO = LazyOptional.of(() -> this.tier);
        graph = new ConnectionGraph(pos);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        tierLO.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level != null && level.isClientSide) renderBounds = super.getRenderBoundingBox();
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);

        this.totalEnergyTransferred = tag.getLong(EnergyNodeConstants.NBT_TOTAL_ENERGY_TRANSFERRED_KEY);
        this.transferredThisTick = tag.getLong(EnergyNodeConstants.NBT_TRANSFERRED_THIS_TICK_KEY);
        this.tier.setTier(Registration.TIERS.getEntries()
                .stream()
                .map(RegistryObject::get)
                .filter(tier -> tier.getSerializedName().equals(tag.getString(EnergyNodeConstants.NBT_TIER)))
                .findFirst()
                .orElse(Registration.BASE.get()));
        this.totalEnergyTransferredLastTick = this.totalEnergyTransferred;
        this.graph.deserializeNBT(tag.getCompound("graph"));

        if (level != null && level.isClientSide) rebuildRenderBounds();

    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putLong(EnergyNodeConstants.NBT_TOTAL_ENERGY_TRANSFERRED_KEY, this.totalEnergyTransferred);
        tag.putLong(EnergyNodeConstants.NBT_TRANSFERRED_THIS_TICK_KEY, this.transferredThisTick);
        tag.putString(EnergyNodeConstants.NBT_TIER, this.tier.getSerializedName());
        tag.put("graph", this.graph.serializeNBT());

    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == Registration.TIER_CAPABILITY)
            return tierLO.cast();

        return super.getCapability(cap, side);
    }

    public ConnectionGraph getGraph() {
        return graph;
    }

    static public void tick(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull BlockEntity tile) {
        EnergyControllerTile ECTile = (EnergyControllerTile) tile;
        if (!level.isClientSide) {
            // Compute the FE transfer in this tick by taking the difference between total transfer this
            // tick and the total transfer last tick
            ECTile.transferredThisTick = Math.abs(ECTile.totalEnergyTransferred);

            ECTile.totalEnergyTransferred = 0;
            if (ECTile.transferredThisTick > 0) {
                ECTile.setChanged();
            }

            if (ECTile.ticks % 10 == 0) {
                ECTile.setChanged();
            }
        } else {
            if (ECTile.ticks % 10 == 0) {
                ECTile.spawnParticles();
            }
        }
        ECTile.ticks = ++ECTile.ticks % 20;
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticles() {
        //if (connectedNodes.size() <= 0) return;
        if (Minecraft.getInstance().player != null
            && level != null
            && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof EnergyLinkerItem) {
            // TODO - Particles overhaul once caps are in
            // TODO - Maybe convert particles into TER code only?
            getGraph().getInputNodes().forEach(input -> {
                Vec3 spawn = Vec3.atCenterOf(input.pos());
                Vec3 dest = Vec3.atCenterOf(worldPosition);
                EnergyNodeParticleData data = new EnergyNodeParticleData(.2f, .5f, 1f);
                level.addParticle(data, spawn.x, spawn.y, spawn.z, dest.x, dest.y, dest.z);
            });

            getGraph().getOutputNodes().forEach(output -> {
                Vec3 spawn = Vec3.atCenterOf(worldPosition);
                Vec3 dest = Vec3.atCenterOf(output.pos());
                EnergyNodeParticleData data = new EnergyNodeParticleData(1f, .5f, .1f);
                level.addParticle(data, spawn.x, spawn.y, spawn.z, dest.x, dest.y, dest.z);
            });
        }
    }

    public void rebuildRenderBounds() {
        this.renderBounds = super.getRenderBoundingBox();
        List<BlockPos> allNodes = new ArrayList<>();
        allNodes.addAll(getGraph().getOutputNodes().stream().map(Node::pos).toList());
        allNodes.addAll(getGraph().getInputNodes().stream().map(Node::pos).toList());

        //List<BlockPos> blockPos = getGraph().getNodeGraph().nodes().stream().map(Node::pos).toList();

        for (BlockPos nodePos : allNodes) {
            AABB aabbNodePos = AABB.ofSize(Vec3.atCenterOf(nodePos), 1, 1, 1);
            renderBounds = getRenderBoundingBox().minmax(aabbNodePos);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return renderBounds != null ? renderBounds : super.getRenderBoundingBox();
    }

    public void setTier(IControllerTier tier) {
        this.tier = tier;
        this.tierLO = LazyOptional.of(() -> tier);
    }

    @Override
    public IControllerTier getTier() {
        return tier;
    }

    public void setChanged() {
        super.setChanged();
    }
}
