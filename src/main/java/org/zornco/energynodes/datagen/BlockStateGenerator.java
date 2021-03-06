package org.zornco.energynodes.datagen;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;

import java.util.Objects;

public class BlockStateGenerator extends BlockStateProvider {
    public BlockStateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, EnergyNodes.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile controllerModel = models().orientable(Objects.requireNonNull(Registration.ENERGY_CONTROLLER_BLOCK.get().getRegistryName()).getPath(),
                new ResourceLocation(EnergyNodes.MOD_ID, "block/controller_side"),
                new ResourceLocation(EnergyNodes.MOD_ID, "block/controller_screen"),
                new ResourceLocation(EnergyNodes.MOD_ID, "block/controller_side")
        );
        horizontalBlock(Registration.ENERGY_CONTROLLER_BLOCK.get(), controllerModel);
        simpleBlockItem(Registration.ENERGY_CONTROLLER_BLOCK.get(), controllerModel);
        simpleBlock(Registration.INPUT_NODE_BLOCK.get());
        simpleBlockItem(Registration.INPUT_NODE_BLOCK.get(),cubeAll(Registration.INPUT_NODE_BLOCK.get()));
        simpleBlock(Registration.OUTPUT_NODE_BLOCK.get());
        simpleBlockItem(Registration.OUTPUT_NODE_BLOCK.get(),cubeAll(Registration.OUTPUT_NODE_BLOCK.get()));
    }
}
