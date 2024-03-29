package org.zornco.energynodes.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.block.BaseNodeBlock;
import org.zornco.energynodes.graph.ConnectionGraph;
import org.zornco.energynodes.graph.Node;
import org.zornco.energynodes.item.EnergyLinkerItem;
import org.zornco.energynodes.particles.EnergyNodeParticleData;
import org.zornco.energynodes.tiers.ControllerTier;
import org.zornco.energynodes.tiers.IControllerTier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class BaseControllerTile extends BlockEntity implements IControllerTile {
    protected final ConnectionGraph graph;
    public int ticks = 0;
    public HashMap<BlockPos, BaseNodeTile> cachedOutputs = new HashMap<>();
    public long transferredThisTick;
    public int lastTransferredRequest;
    protected IControllerTier tier;
    protected LazyOptional<IControllerTier> tierLO;
    private AABB renderBounds;

    public BaseControllerTile(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_, @Nonnull BlockPos pos) {
        super(p_155228_, p_155229_, p_155230_);
        this.tier = new ControllerTier();
        graph = new ConnectionGraph(pos);
        tierLO = LazyOptional.of(() -> this.tier);
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

        this.tier.setTier(Registration.TIERS.getEntries()
            .stream()
            .map(RegistryObject::get)
            .filter(tier -> tier.getSerializedName().equals(tag.getString(EnergyNodeConstants.NBT_TIER)))
            .findFirst()
            .orElse(Registration.BASE.get()));

        this.graph.deserializeNBT(tag.getCompound("graph"));

        if (level != null && level.isClientSide) rebuildRenderBounds();
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
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

    @SuppressWarnings("unused")
    static public void tickCommon(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull BlockEntity tile) {
        BaseControllerTile BCTile = (BaseControllerTile) tile;
        BCTile.tickAdditional(level);
        BCTile.updateCachedOutputs(level);
        if (level.isClientSide) {
            if (BCTile.ticks % 10 == 0) {
                BCTile.spawnParticles();
            }
        }
        BCTile.ticks = ++BCTile.ticks % 20;
    }

    public void tickAdditional(@Nonnull Level level) {}

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

    @OnlyIn(Dist.CLIENT)
    protected void spawnParticles() {
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

    protected void updateCachedOutputs(@Nonnull Level level) {
        if (ticks % 10 == 0) {
            /*level.getBlockEntity( entry.getKey())*/
            cachedOutputs.entrySet().stream().filter(entry -> entry.getValue() == null).forEach(entry -> cachedOutputs.remove(entry.getKey()));
            List<Node> blockEntities = getGraph().getOutputNodes().stream().filter(nodePos -> !cachedOutputs.containsKey(nodePos.pos())).toList();
            blockEntities.forEach(n -> cachedOutputs.put(n.pos(), (BaseNodeTile) level.getBlockEntity(n.pos())));

            setChanged();
        }
    }

    @Override
    public int receiveInput(BaseNodeTile nodeTile, Object maxReceive, boolean simulate) {
        return 0;
    }

//    @Nonnull
//    public static List<HashMap<Direction, BlockEntity>> getValidDestinations(Stream<BaseNodeTile> values) {
//        return values
//            .filter(Objects::nonNull)
//            .map(BaseNodeTile::getConnectedTiles)
//            .filter(map -> !map.isEmpty())
//            .toList();
//    }
//
//    @Nonnull
//    @SuppressWarnings("unchecked")
//    public static <T> List<LazyOptional<T>> getOptionals(@NotNull Capability<T> cap, List<HashMap<Direction,
//                                                     BlockEntity>> validDestinations, Predicate<T> storagePredicate) {
//        List<LazyOptional<T>> result = new ArrayList<>();
//        for (var entry : validDestinations) {
//            for (var capMap: entry.entrySet()) {
//                if (capMap.getValue() == null) continue;
//                result.add(capMap.getValue().getCapability(cap, capMap.getKey().getOpposite()).cast());
//            }
//        }
//        return result;
//
//
////        return validDestinations.stream()
////            .map(set -> set.entrySet().stream()
////                .map(capMap -> {
////                        if (capMap.getValue() != null) {
////                            return capMap.getValue().getCapability(cap, capMap.getKey().getOpposite())
////                                .map(storage -> ((T) storage))
////                                .filter(storagePredicate);
////                        } else return Optional.<T>empty();
////                    }
////                )
////                .filter(Optional::isPresent)
////                .toList())
////            .toList().stream()
////            .flatMap(List::stream)
////            .toList();
//    }

    @Override
    public boolean canReceiveInput(BaseNodeTile nodeTile) {
        return getGraph().getNode(BaseNodeBlock.Flow.IN, nodeTile.getBlockPos()) != null;
    }
}
