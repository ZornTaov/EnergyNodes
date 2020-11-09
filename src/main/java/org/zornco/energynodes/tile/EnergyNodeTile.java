package org.zornco.energynodes.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.capability.NodeEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class EnergyNodeTile extends TileEntity {
    private static final String NBT_CONTROLLER_POS_KEY = "controller-pos";
    private static final String NBT_CONNECTED_TILES_KEY = "connected-tiles";
    public NodeEnergyStorage energyStorage;
    private final LazyOptional<NodeEnergyStorage> energy;
    public final HashMap<BlockPos,TileEntity> connectedTiles = new HashMap<>();

    @Nullable
    public BlockPos controllerPos;

    public EnergyNodeTile() {
        super(Registration.ENERGY_TRANSFER_TILE.get());
        this.energyStorage = new NodeEnergyStorage(this);
        this.energy = LazyOptional.of(() -> this.energyStorage);
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT tag) {
        super.read(state, tag);
        if (tag.get(NBT_CONTROLLER_POS_KEY) != null)
            BlockPos.CODEC.decode(NBTDynamicOps.INSTANCE, tag.get(NBT_CONTROLLER_POS_KEY))
                    .resultOrPartial(EnergyNodes.LOGGER::error)
                    .ifPresent(blockPosINBTPair -> this.controllerPos = blockPosINBTPair.getFirst());
        if (tag.get(NBT_CONNECTED_TILES_KEY) != null)
            Utils.LBPCODEC.decode(NBTDynamicOps.INSTANCE, tag.getList(NBT_CONNECTED_TILES_KEY, Constants.NBT.TAG_INT_ARRAY))
                    .resultOrPartial(EnergyNodes.LOGGER::error)
                    .ifPresent(listINBTPair -> listINBTPair.getFirst().forEach(blockPos -> connectedTiles.put(blockPos,
                            Objects.requireNonNull(getWorld()).getTileEntity(blockPos))));
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        CompoundNBT tag = super.write(compound);
        if (controllerPos != null)
            BlockPos.CODEC.encodeStart(NBTDynamicOps.INSTANCE, controllerPos)
                    .resultOrPartial(EnergyNodes.LOGGER::error)
                    .ifPresent(inbt -> tag.put(NBT_CONTROLLER_POS_KEY, inbt));
        if (getBlockState().get(EnergyNodeBlock.PROP_INOUT) == EnergyNodeBlock.Flow.OUT && connectedTiles.size() != 0)
            Utils.LBPCODEC.encodeStart(NBTDynamicOps.INSTANCE, new ArrayList<>(connectedTiles.keySet()))
                    .resultOrPartial(EnergyNodes.LOGGER::error)
                    .ifPresent(inbt -> tag.put(NBT_CONNECTED_TILES_KEY, inbt));
        return tag;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        read(state, tag);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet){
        this.handleUpdateTag(this.getBlockState(), packet.getNbtCompound());
        ModelDataManager.requestModelDataRefresh(this);
        Objects.requireNonNull(this.getWorld()).markBlockRangeForRenderUpdate(this.pos, this.getBlockState(), this.getBlockState());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY)
            return energy.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void remove() {
        energy.invalidate();
        super.remove();
    }
}
