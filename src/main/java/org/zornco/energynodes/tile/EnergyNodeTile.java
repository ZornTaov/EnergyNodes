package org.zornco.energynodes.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.capability.NodeEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class EnergyNodeTile extends TileEntity {
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
    public void onLoad() {
        if (level != null && !level.isClientSide)
        {
            MinecraftServer server = level.getServer();
            if (server != null) {
                server.tell(new TickDelayedTask(server.getTickCount() + 5, this::LoadConnectedTiles));
            }
        }
    }

    @Override
    public void load(@Nonnull BlockState state, @Nonnull CompoundNBT tag) {
        super.load(state, tag);
        if (tag.get(EnergyNodeConstants.NBT_CONTROLLER_POS_KEY) != null)
            BlockPos.CODEC.decode(NBTDynamicOps.INSTANCE, tag.get(EnergyNodeConstants.NBT_CONTROLLER_POS_KEY))
                    .resultOrPartial(EnergyNodes.LOGGER::error)
                    .ifPresent(blockPosINBTPair -> this.controllerPos = blockPosINBTPair.getFirst());
        if (tag.get(EnergyNodeConstants.NBT_CONNECTED_TILES_KEY) != null)
            Utils.BLOCK_POS_LIST_CODEC.decode(NBTDynamicOps.INSTANCE, tag.getList(EnergyNodeConstants.NBT_CONNECTED_TILES_KEY, Constants.NBT.TAG_INT_ARRAY))
                    .resultOrPartial(EnergyNodes.LOGGER::error)
                    .ifPresent(listINBTPair -> listINBTPair.getFirst().forEach(blockPos -> connectedTiles.put(blockPos,
                            null)));
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT compound) {
        CompoundNBT tag = super.save(compound);
        if (controllerPos != null)
            BlockPos.CODEC.encodeStart(NBTDynamicOps.INSTANCE, controllerPos)
                    .resultOrPartial(EnergyNodes.LOGGER::error)
                    .ifPresent(inbt -> tag.put(EnergyNodeConstants.NBT_CONTROLLER_POS_KEY, inbt));
        if (getBlockState().getValue(EnergyNodeBlock.PROP_INOUT) == EnergyNodeBlock.Flow.OUT && connectedTiles.size() != 0)
            Utils.BLOCK_POS_LIST_CODEC.encodeStart(NBTDynamicOps.INSTANCE, new ArrayList<>(connectedTiles.keySet()))
                    .resultOrPartial(EnergyNodes.LOGGER::error)
                    .ifPresent(inbt -> tag.put(EnergyNodeConstants.NBT_CONNECTED_TILES_KEY, inbt));
        return tag;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        load(state, tag);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet){
        this.handleUpdateTag(this.getBlockState(), packet.getTag());
        ModelDataManager.requestModelDataRefresh(this);
        Objects.requireNonNull(this.getLevel()).setBlocksDirty(this.worldPosition, this.getBlockState(), this.getBlockState());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY)
            return energy.cast();

        return super.getCapability(cap, side);
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        energy.invalidate();
    }

    private void LoadConnectedTiles() {
        if (level != null) {

            if (controllerPos != null && level.isLoaded(controllerPos)) {
                this.energyStorage.setController((EnergyControllerTile) level.getBlockEntity(controllerPos));
            }
            for (BlockPos ctPos : connectedTiles.keySet()) {
                if (level.isLoaded(ctPos)) {
                    connectedTiles.replace(ctPos, level.getBlockEntity(ctPos));
                } else {
                    connectedTiles.remove(ctPos);
                }
            }
        }
    }
}
