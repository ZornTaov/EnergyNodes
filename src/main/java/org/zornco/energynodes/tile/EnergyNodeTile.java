package org.zornco.energynodes.tile;

import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.core.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.block.IControllerNode;
import org.zornco.energynodes.block.INodeTile;
import org.zornco.energynodes.capability.NodeEnergyStorage;
import org.zornco.energynodes.graph.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class EnergyNodeTile extends BlockEntity implements INodeTile {
    public final NodeEnergyStorage energyStorage;
    private final LazyOptional<NodeEnergyStorage> energy;
    public final HashMap<Direction,BlockEntity> connectedTiles = new HashMap<>();

    @Nullable
    public BlockPos controllerPos;

    private WeakReference<Node> nodeRef = null;

    public EnergyNodeTile(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(Registration.ENERGY_TRANSFER_TILE.get(), pos, state);
        this.energyStorage = new NodeEnergyStorage(this);
        this.energy = LazyOptional.of(() -> this.energyStorage);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
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
        if (cap == ForgeCapabilities.ENERGY)
            return energy.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energy.invalidate();
    }

    @Override
    public WeakReference<Node> getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(WeakReference<Node> nodeRef) {
        this.nodeRef = nodeRef;
    }

    @Override
    public void clearConnection() {

        this.setNodeRef(null);
        this.controllerPos = null;
        this.energyStorage.setController(null);
        this.energyStorage.setEnergyStored(0);
    }

    @Override
    public void connectController(IControllerNode controller) {

        this.controllerPos = controller.getBlockPos().subtract(this.getBlockPos());
        this.energyStorage.setController((EnergyControllerTile) controller);
    }

    @Override
    public Capability<?> getCapabilityType() {
        return ForgeCapabilities.ENERGY;
    }
}
