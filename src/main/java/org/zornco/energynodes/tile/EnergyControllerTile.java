package org.zornco.energynodes.tile;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.common.util.Constants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnergyControllerTile extends TileEntity {

    protected static final String NBT_CONNECTED_NODES_KEY = "connected-nodes";
    protected static final String NBT_TOTAL_ENERGY_TRANSFERRED_KEY = "total-energy-transferred";
    protected static final String NBT_RATE_LIMIT_KEY = "rate-limit";

    public static final int UNLIMITED_RATE = -1;
    protected int rateLimit = UNLIMITED_RATE;

    protected int ticks = 0;
    protected long totalEnergyTransferred = 0;
    protected long totalEnergyTransferredLastTick = 0;
    protected float transferRate = 0;
    protected float lastTransferRateSent = 0;
    protected int ticksSinceLastTransferRatePacket = 0;
    public List<BlockPos> connectedNodes = new ArrayList<>();
    private BlockPos[] outputSides;
    private final Codec<List<BlockPos>> codec = Codec.list(BlockPos.CODEC);


    public EnergyControllerTile() {
        super(Registration.ENERGY_CONTROLLER_TILE.get());
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT tag) {
        super.read(state, tag);
        codec.decode(NBTDynamicOps.INSTANCE, tag.getList(NBT_CONNECTED_NODES_KEY, Constants.NBT.TAG_INT_ARRAY))
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(listINBTPair -> this.connectedNodes = new ArrayList<>(listINBTPair.getFirst()));
        this.totalEnergyTransferred = tag.getLong(NBT_TOTAL_ENERGY_TRANSFERRED_KEY);
        this.rateLimit = tag.getInt(NBT_RATE_LIMIT_KEY);

        this.totalEnergyTransferredLastTick = this.totalEnergyTransferred;
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        CompoundNBT tag = super.write(compound);
        codec.encodeStart(NBTDynamicOps.INSTANCE, connectedNodes)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(inbt -> tag.put(NBT_CONNECTED_NODES_KEY, inbt));
        tag.putLong(NBT_TOTAL_ENERGY_TRANSFERRED_KEY, this.totalEnergyTransferred);
        tag.putInt(NBT_RATE_LIMIT_KEY, this.rateLimit);
        return tag;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();
        codec.encodeStart(NBTDynamicOps.INSTANCE, connectedNodes)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(inbt -> tag.put(NBT_CONNECTED_NODES_KEY, inbt));
        tag.putLong(NBT_TOTAL_ENERGY_TRANSFERRED_KEY, this.totalEnergyTransferred);
        tag.putInt(NBT_RATE_LIMIT_KEY, this.rateLimit);
        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        codec.decode(NBTDynamicOps.INSTANCE, tag.getList(NBT_CONNECTED_NODES_KEY, Constants.NBT.TAG_INT_ARRAY))
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(listINBTPair -> this.connectedNodes = new ArrayList<>(listINBTPair.getFirst()));
        this.totalEnergyTransferred = tag.getLong(NBT_TOTAL_ENERGY_TRANSFERRED_KEY);
        this.rateLimit = tag.getInt(NBT_RATE_LIMIT_KEY);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 1, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet){
        this.handleUpdateTag(this.world.getBlockState(pos), packet.getNbtCompound());
        ModelDataManager.requestModelDataRefresh(this);
        this.getWorld().markBlockRangeForRenderUpdate(this.pos, this.getBlockState(), this.getBlockState());
    }
}
