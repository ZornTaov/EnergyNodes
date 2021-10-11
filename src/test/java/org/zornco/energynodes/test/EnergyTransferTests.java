package org.zornco.energynodes.test;

import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"unused", "SameParameterValue"})
@IntegrationTestClass("transfersetup")
public class EnergyTransferTests {

    @IntegrationTest("basic_c")
    void transferEnergy(IntegrationTestHelper testHelper) {
        TileEntity mekIn = testHelper.getTileEntity(Vars.mekCubeInPos);
        Assertions.assertNotNull(mekIn);
        TileEntity mekOut = testHelper.getTileEntity(Vars.mekCubeOutPos);
        Assertions.assertNotNull(mekOut);

        testHelper.runAfter(0, () -> Vars.injectEnergy(mekIn
                .getCapability(CapabilityEnergy.ENERGY, Direction.UP), 50))
            .thenRun(5, () -> mekOut
                .getCapability(CapabilityEnergy.ENERGY, Direction.EAST)
                .ifPresent(out -> {
                    int energyStored = out.getEnergyStored();
                    testHelper.assertTrue(() -> energyStored == 50,
                        "Expected energy to transfer not correct, got: " + energyStored);
                })
            );
    }

    @IntegrationTest("basic_2to2_c")
    void splitEnergy(IntegrationTestHelper testHelper) {
        LazyOptional<IEnergyStorage> mekEnIn = Vars.getMekCap(testHelper, Vars.mekCubeInPos);
        LazyOptional<IEnergyStorage> mekEnOut1 = Vars.getMekCap(testHelper, Vars.mekCubeOutPos);
        LazyOptional<IEnergyStorage> mekEnOut2 = Vars.getMekCap(testHelper, Vars.mekCubeOutPos.relative(Direction.NORTH, 2));

        testHelper
            .runAfter(0, () -> Vars.injectEnergy(mekEnIn, 50))
            .thenRun(5, () -> {
                AtomicInteger total = new AtomicInteger(0);
                Vars.testStored(testHelper, mekEnOut1, total, 25);
                Vars.testStored(testHelper, mekEnOut2, total, 25);

                testHelper.assertTrue(() -> total.get() == 50, "Expected 50 transfer total.");
            });
    }

    @IntegrationTest("basic_2to2_c")
    void overchargeEnergy(IntegrationTestHelper testHelper) {
        LazyOptional<IEnergyStorage> mekEnIn = Vars.getMekCap(testHelper, Vars.mekCubeInPos);
        LazyOptional<IEnergyStorage> mekEnOut1 = Vars.getMekCap(testHelper, Vars.mekCubeOutPos);
        LazyOptional<IEnergyStorage> mekEnOut2 = Vars.getMekCap(testHelper, Vars.mekCubeOutPos.relative(Direction.NORTH, 2));

        testHelper
            .runAfter(0, () -> Vars.injectEnergy(mekEnIn, 1000))
            .thenRun(3, () -> {
                AtomicInteger total = new AtomicInteger(0);
                Vars.testStored(testHelper, mekEnOut1, total, 400);
                Vars.testStored(testHelper, mekEnOut2, total, 400);

                testHelper.assertTrue(() -> total.get() == 800, "Expected 800 transfer total.");
            }).thenRun(5, () -> {
                AtomicInteger total = new AtomicInteger(0);
                Vars.testStored(testHelper, mekEnOut1, total, 500);
                Vars.testStored(testHelper, mekEnOut2, total, 500);

                testHelper.assertTrue(() -> total.get() == 1000, "Expected 1000 transfer total.");
            });
    }

    @IntegrationTest("advanced_2to1_c")
    void combineEnergy(IntegrationTestHelper testHelper) {
        LazyOptional<IEnergyStorage> mekEnIn = Vars.getMekCap(testHelper, Vars.mekCubeInPos);
        LazyOptional<IEnergyStorage> mekEnIn2 = Vars.getMekCap(testHelper, Vars.mekCubeInPos.relative(Direction.UP));
        LazyOptional<IEnergyStorage> mekEnOut = Vars.getMekCap(testHelper, Vars.mekCubeOutPos);

        testHelper
            .runAfter(0, () -> {
                Vars.injectEnergy(mekEnIn, 50);
                Vars.injectEnergy(mekEnIn2, 50);
            })
            .thenRun(5, () -> {
                AtomicInteger total = new AtomicInteger(0);
                Vars.testStored(testHelper, mekEnOut, total, 100);
            });
    }

    @IntegrationTest("basic_2to2_c")
    void crossEnergyBasic(IntegrationTestHelper testHelper) {
        LazyOptional<IEnergyStorage> mekEnIn = Vars.getMekCap(testHelper, Vars.mekCubeInPos);
        LazyOptional<IEnergyStorage> mekEnIn2 = Vars.getMekCap(testHelper, Vars.mekCubeInPos.relative(Direction.NORTH, 2));
        LazyOptional<IEnergyStorage> mekEnOut1 = Vars.getMekCap(testHelper, Vars.mekCubeOutPos);
        LazyOptional<IEnergyStorage> mekEnOut2 = Vars.getMekCap(testHelper, Vars.mekCubeOutPos.relative(Direction.NORTH, 2));

        testHelper
            .runAfter(0, () -> {
                Vars.injectEnergy(mekEnIn, 8000);
                Vars.injectEnergy(mekEnIn2, 8000);
            })
            .thenRun(2, () -> {
                AtomicInteger total = new AtomicInteger(0);
                Vars.testStored(testHelper, mekEnOut1, total, 400);
                Vars.testStored(testHelper, mekEnOut2, total, 400);

                testHelper.assertTrue(() -> total.get() == 800, "Expected "+800+" transfer total. Got: "+total.get());
            });
    }

    @IntegrationTest("advanced_2to2_c")
    void crossEnergyAdvanced(IntegrationTestHelper testHelper) {
        LazyOptional<IEnergyStorage> mekEnIn = Vars.getMekCap(testHelper, Vars.mekCubeInPos);
        LazyOptional<IEnergyStorage> mekEnIn2 = Vars.getMekCap(testHelper, Vars.mekCubeInPos.relative(Direction.UP));
        LazyOptional<IEnergyStorage> mekEnOut1 = Vars.getMekCap(testHelper, Vars.mekCubeOutPos);
        LazyOptional<IEnergyStorage> mekEnOut2 = Vars.getMekCap(testHelper, Vars.mekCubeOutPos.relative(Direction.UP));

        testHelper
            .runAfter(0, () -> {
                Vars.injectEnergy(mekEnIn, 50);
                Vars.injectEnergy(mekEnIn2, 50);
            })
            .thenRun(5, () -> {
                AtomicInteger total = new AtomicInteger(0);
                Vars.testStored(testHelper, mekEnOut1, total, 50);
                Vars.testStored(testHelper, mekEnOut2, total, 50);

                testHelper.assertTrue(() -> total.get() == 100, "Expected 100 transfer total.");
            });
    }
}
