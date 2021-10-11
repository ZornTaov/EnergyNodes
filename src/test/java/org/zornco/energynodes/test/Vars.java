package org.zornco.energynodes.test;

import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.zornco.energynodes.Registration;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
@IntegrationTestClass("setup")
public class Vars {

    protected static final BlockPos inNodePos;
    protected static final BlockPos outNodePos;
    protected static final BlockPos controllerPos;
    protected static final BlockPos mekCubeInPos;
    protected static final BlockPos mekCubeOutPos;

    static {
        inNodePos = new BlockPos(4, 0, 1);
        outNodePos = new BlockPos(0, 0, 1);
        controllerPos = new BlockPos(2, 0, 1);

        mekCubeInPos = new BlockPos(4, 0, 2);
        mekCubeOutPos = new BlockPos(0, 0, 2);
    }
    protected static void linkNodes(IntegrationTestHelper testHelper, BlockPos nodePos) {
        ItemStack linker = new ItemStack(Registration.ENERGY_LINKER_ITEM.get());
        // link node to controller
        testHelper.useItem(nodePos, Direction.UP, linker);
        testHelper.useItem(controllerPos, Direction.UP, linker);
    }

    @Nonnull
    static LazyOptional<IEnergyStorage> getMekCap(IntegrationTestHelper testHelper, BlockPos blockPos) {
        TileEntity mekIn = testHelper.getTileEntity(blockPos);
        Assertions.assertNotNull(mekIn);
        return mekIn.getCapability(CapabilityEnergy.ENERGY, Direction.UP);
    }

    static void injectEnergy(LazyOptional<IEnergyStorage> mekEnIn, int amount) {
        mekEnIn.ifPresent(in -> in.receiveEnergy(amount, false));
    }

    static void testStored(IntegrationTestHelper testHelper, LazyOptional<IEnergyStorage> mekEnOut1, AtomicInteger total, int testAgainst) {
        mekEnOut1.ifPresent(out -> {
            int stored = out.getEnergyStored();
            total.getAndAdd(stored);
            testHelper.assertTrue(() -> stored == testAgainst, "Expected "+testAgainst+" energy to transfer, got: " + stored);
        });
    }

    @IntegrationTest("setup")
    void setup(IntegrationTestHelper testHelper)
    {

    }
}
