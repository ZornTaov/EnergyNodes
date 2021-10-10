package org.zornco.energynodes.test;

import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import org.junit.jupiter.api.Assertions;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.tile.EnergyControllerTile;
import org.zornco.energynodes.tile.EnergyNodeTile;

@SuppressWarnings("unused")
@IntegrationTestClass("nodesetup")
public class NodeSetupTests {


    private static final BlockPos inNodePos;
    private static final BlockPos outNodePos;
    private static final BlockPos controllerPos;
    private static final BlockPos mekCubeInPos;
    private static final BlockPos mekCubeOutPos;
    private static ItemStack linker;

    static {
        inNodePos = new BlockPos(4, 0, 1);
        outNodePos = new BlockPos(0, 0, 1);
        controllerPos = new BlockPos(2, 0, 1);

        mekCubeInPos = new BlockPos(4, 0, 2);
        mekCubeOutPos = new BlockPos(0, 0, 2);
    }

    @IntegrationTest("basic")
    void nodesHaveCorrectTiles(IntegrationTestHelper testHelper) {
        testHelper.assertTileEntityAt(inNodePos, EnergyNodeTile.class, "Input Node Tile missing.");
        testHelper.assertTileEntityAt(outNodePos, EnergyNodeTile.class, "Output Node Tile missing.");
        testHelper.assertTileEntityAt(controllerPos, EnergyControllerTile.class, "Controller Tile missing.");
    }

    @IntegrationTest("basic")
    void usingLinkerOnNodeCopiesPosition(IntegrationTestHelper testHelper) {
        linker = new ItemStack(Registration.ENERGY_LINKER_ITEM.get());
        testHelper.useItem(inNodePos, Direction.UP, linker);
        testHelper.assertTrue(linker::hasTag, "Expected NBT on Linker.");
        CompoundNBT linkerTag = linker.getTag();
        Assertions.assertNotNull(linkerTag);
        testHelper.assertTrue(() -> linkerTag.contains(EnergyNodeConstants.NBT_NODE_POS_KEY), "Linker not linked.");
    }

    @IntegrationTest("basic")
    void canLinkNodes(IntegrationTestHelper testHelper) {
        linkNodes(testHelper);
    }

    private void linkNodes(IntegrationTestHelper testHelper) {
        linker = new ItemStack(Registration.ENERGY_LINKER_ITEM.get());
        // link in to controller
        testHelper.useItem(inNodePos, Direction.UP, linker);
        testHelper.useItem(controllerPos, Direction.UP, linker);

        // link out to controller
        testHelper.useItem(outNodePos, Direction.UP, linker);
        testHelper.useItem(controllerPos, Direction.UP, linker);
    }

    @IntegrationTest("basic")
    void transferEnergy(IntegrationTestHelper testHelper) {
        linkNodes(testHelper);
        TileEntity mekIn = testHelper.getTileEntity(mekCubeInPos);
        Assertions.assertNotNull(mekIn);
        TileEntity mekOut = testHelper.getTileEntity(mekCubeOutPos);
        Assertions.assertNotNull(mekOut);

        testHelper.runAfter(0, () -> mekIn
                .getCapability(CapabilityEnergy.ENERGY, Direction.UP)
                .ifPresent(in -> in
                    .receiveEnergy(50, false)))
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
    void stillLinked(IntegrationTestHelper testHelper) {
        EnergyControllerTile controller = (EnergyControllerTile) testHelper.getTileEntity(controllerPos);
        Assertions.assertNotNull(controller);
        EnergyNodeTile nodeIn = (EnergyNodeTile) testHelper.getTileEntity(inNodePos);
        Assertions.assertNotNull(nodeIn);
        EnergyNodeTile nodeOut = (EnergyNodeTile) testHelper.getTileEntity(outNodePos);
        Assertions.assertNotNull(nodeOut);

        BlockPos inOffset = inNodePos.subtract(controllerPos);
        BlockPos outOffset = outNodePos.subtract(controllerPos);
        testHelper.assertTrue(() -> controller.connectedNodes.contains(inOffset) && controller.connectedNodes.contains(outOffset),"Controller not connected");
        testHelper.assertTrue(() -> {
            assert nodeIn.controllerPos != null;
            BlockPos controllerOffset = nodeIn.controllerPos.offset(inNodePos);
            return controllerOffset.asLong() == controllerPos.asLong();
        }, "In Node not connected.");
    }
}
