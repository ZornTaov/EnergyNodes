package org.zornco.energynodes.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.block.NodeBlockTags;

import java.util.Set;

public class BlockTagGenerator extends BlockTagsProvider {

    public BlockTagGenerator(DataGenerator generator, ExistingFileHelper files) {
        super(generator, EnergyNodes.MOD_ID, files);
    }

    @Override
    protected void addTags() {

        var blocks = Set.of(
            Registration.ENERGY_CONTROLLER_BLOCK.get(),
            Registration.INPUT_ENERGY_BLOCK.get(),
            Registration.OUTPUT_ENERGY_BLOCK.get(),
            Registration.FLUID_CONTROLLER_BLOCK.get(),
            Registration.INPUT_FLUID_BLOCK.get(),
            Registration.OUTPUT_FLUID_BLOCK.get()
        );
        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var ironTool = tag(BlockTags.NEEDS_IRON_TOOL);

        blocks.forEach(block -> {
            pickaxe.add(block);
            ironTool.add(block);
        });

        var controller = tag(NodeBlockTags.CONTROLLER_TAG);
        var node = tag(NodeBlockTags.NODE_TAG);
        var energy = tag(NodeBlockTags.ENERGY_TAG);
        var fluid = tag(NodeBlockTags.FLUID_TAG);

        controller.add(Registration.ENERGY_CONTROLLER_BLOCK.get());
        node.add(Registration.INPUT_ENERGY_BLOCK.get());
        node.add(Registration.OUTPUT_ENERGY_BLOCK.get());
        energy.add(Registration.ENERGY_CONTROLLER_BLOCK.get());
        energy.add(Registration.INPUT_ENERGY_BLOCK.get());
        energy.add(Registration.OUTPUT_ENERGY_BLOCK.get());

        controller.add(Registration.FLUID_CONTROLLER_BLOCK.get());
        node.add(Registration.INPUT_FLUID_BLOCK.get());
        node.add(Registration.OUTPUT_FLUID_BLOCK.get());
        fluid.add(Registration.FLUID_CONTROLLER_BLOCK.get());
        fluid.add(Registration.INPUT_FLUID_BLOCK.get());
        fluid.add(Registration.OUTPUT_FLUID_BLOCK.get());
    }
}
