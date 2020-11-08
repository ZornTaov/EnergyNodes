package org.zornco.energynodes.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
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

public class EnergyNodeTile extends TileEntity {
    private static final String NBT_CONTROLLER_POS_KEY = "controller-pos";
    public BlockPos controllerPos;

    public EnergyNodeTile() {
        super(Registration.ENERGY_TRANSFER_TILE.get());
    }
    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT tag) {
        super.read(state, tag);
        BlockPos.CODEC.decode(NBTDynamicOps.INSTANCE, tag.get(NBT_CONTROLLER_POS_KEY))
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(listINBTPair -> this.controllerPos = listINBTPair.getFirst());
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        CompoundNBT tag = super.write(compound);
        BlockPos.CODEC.encodeStart(NBTDynamicOps.INSTANCE, controllerPos)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(inbt -> tag.put(NBT_CONTROLLER_POS_KEY, inbt));
        return tag;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();
        BlockPos.CODEC.encodeStart(NBTDynamicOps.INSTANCE, controllerPos)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(inbt -> tag.put(NBT_CONTROLLER_POS_KEY, inbt));
        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        BlockPos.CODEC.decode(NBTDynamicOps.INSTANCE, tag.get(NBT_CONTROLLER_POS_KEY))
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(listINBTPair -> this.controllerPos = listINBTPair.getFirst());
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
