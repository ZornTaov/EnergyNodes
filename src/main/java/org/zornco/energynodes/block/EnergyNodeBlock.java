package org.zornco.energynodes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import org.zornco.energynodes.tile.EnergyNodeTile;

import javax.annotation.Nullable;

public class EnergyNodeBlock extends Block {
    public static class Flow {
        public static final boolean OUT = true;
        public static final boolean IN = false;
    }
    public static final BooleanProperty PROP_INOUT = BooleanProperty.create("inout");
    public EnergyNodeBlock(Properties properties, boolean flow) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(PROP_INOUT, flow));
    }
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(PROP_INOUT);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnergyNodeTile();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
}
