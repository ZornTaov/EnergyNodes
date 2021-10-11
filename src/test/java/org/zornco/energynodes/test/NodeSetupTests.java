package org.zornco.energynodes.test;

import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.tile.EnergyControllerTile;
import org.zornco.energynodes.tile.EnergyNodeTile;

@SuppressWarnings("unused")
@IntegrationTestClass("nodesetup")
public class NodeSetupTests {

    @IntegrationTest("basic")
    void nodesHaveCorrectTiles(IntegrationTestHelper testHelper) {
        testHelper.assertTileEntityAt(Vars.inNodePos, EnergyNodeTile.class, "Input Node Tile missing.");
        testHelper.assertTileEntityAt(Vars.outNodePos, EnergyNodeTile.class, "Output Node Tile missing.");
        testHelper.assertTileEntityAt(Vars.controllerPos, EnergyControllerTile.class, "Controller Tile missing.");
    }

    @IntegrationTest("basic")
    void usingLinkerOnNodeCopiesPosition(IntegrationTestHelper testHelper) {
        ItemStack linker = new ItemStack(Registration.ENERGY_LINKER_ITEM.get());
        testHelper.useItem(Vars.inNodePos, Direction.UP, linker);
        testHelper.assertTrue(linker::hasTag, "Expected NBT on Linker.");
        CompoundNBT linkerTag = linker.getTag();
        Assertions.assertNotNull(linkerTag);
        testHelper.assertTrue(() -> linkerTag.contains(EnergyNodeConstants.NBT_NODE_POS_KEY), "Linker not linked.");
    }

    @IntegrationTest("basic")
    void canLinkNodes(IntegrationTestHelper testHelper) {
        Vars.linkNodes(testHelper, Vars.inNodePos);
        Vars.linkNodes(testHelper, Vars.outNodePos);
    }

    @IntegrationTest("basic_2to2_c")
    void stillLinked(IntegrationTestHelper testHelper) {
        EnergyControllerTile controller = (EnergyControllerTile) testHelper.getTileEntity(Vars.controllerPos);
        Assertions.assertNotNull(controller);
        EnergyNodeTile nodeIn = (EnergyNodeTile) testHelper.getTileEntity(Vars.inNodePos);
        Assertions.assertNotNull(nodeIn);
        EnergyNodeTile nodeOut = (EnergyNodeTile) testHelper.getTileEntity(Vars.outNodePos);
        Assertions.assertNotNull(nodeOut);

        BlockPos inOffset = Vars.inNodePos.subtract(Vars.controllerPos);
        BlockPos outOffset = Vars.outNodePos.subtract(Vars.controllerPos);
        testHelper.assertTrue(() -> controller.connectedNodes.contains(inOffset) && controller.connectedNodes.contains(outOffset),"Controller not connected");
        testHelper.assertTrue(() -> {
            assert nodeIn.controllerPos != null;
            BlockPos controllerOffset = nodeIn.controllerPos.offset(Vars.inNodePos);
            return controllerOffset.asLong() == Vars.controllerPos.asLong();
        }, "In Node not connected.");
    }
}
