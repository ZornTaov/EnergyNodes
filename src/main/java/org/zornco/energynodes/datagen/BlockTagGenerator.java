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

        var blocks = Set.of(Registration.ENERGY_CONTROLLER_BLOCK.get(),
            Registration.INPUT_NODE_BLOCK.get(),
            Registration.OUTPUT_NODE_BLOCK.get());
        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var ironTool = tag(BlockTags.NEEDS_IRON_TOOL);

        blocks.forEach(block -> {
            pickaxe.add(block);
            ironTool.add(block);
        });

        var controller = tag(NodeBlockTags.CONTROLLER_TAG);
        var node = tag(NodeBlockTags.NODE_TAG);
        controller.add(Registration.ENERGY_CONTROLLER_BLOCK.get());
        node.add(Registration.INPUT_NODE_BLOCK.get());
        node.add(Registration.OUTPUT_NODE_BLOCK.get());
    }
}
