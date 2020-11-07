package org.zornco.energynodes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;

import javax.annotation.Nullable;

public class EnergyTransferBlock extends Block {
    public static class Flow {
        public static final boolean OUT = true;
        public static final boolean IN = false;
    }
    public static final BooleanProperty PROP_INOUT = BooleanProperty.create("inout");
    public EnergyTransferBlock(Properties properties, boolean flow) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(PROP_INOUT, flow));
    }
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(PROP_INOUT);
    }
}
