package org.zornco.energynodes.datagen;

import net.minecraft.resources.ResourceLocation;
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
        ModelFile controllerModel = models().orientable(Objects.requireNonNull(Registration.ENERGY_CONTROLLER_BLOCK.getId()).getPath(),
                new ResourceLocation(EnergyNodes.MOD_ID, "block/controller_side"),
                new ResourceLocation(EnergyNodes.MOD_ID, "block/controller_screen"),
                new ResourceLocation(EnergyNodes.MOD_ID, "block/controller_side")
        );
        horizontalBlock(Registration.ENERGY_CONTROLLER_BLOCK.get(), controllerModel);
        simpleBlockItem(Registration.ENERGY_CONTROLLER_BLOCK.get(), controllerModel);
        simpleBlock(Registration.INPUT_ENERGY_BLOCK.get());
        simpleBlockItem(Registration.INPUT_ENERGY_BLOCK.get(),cubeAll(Registration.INPUT_ENERGY_BLOCK.get()));
        simpleBlock(Registration.OUTPUT_ENERGY_BLOCK.get());
        simpleBlockItem(Registration.OUTPUT_ENERGY_BLOCK.get(),cubeAll(Registration.OUTPUT_ENERGY_BLOCK.get()));
        simpleBlock(Registration.INPUT_FLUID_BLOCK.get());
        simpleBlockItem(Registration.INPUT_FLUID_BLOCK.get(),cubeAll(Registration.INPUT_FLUID_BLOCK.get()));
        simpleBlock(Registration.OUTPUT_FLUID_BLOCK.get());
        simpleBlockItem(Registration.OUTPUT_FLUID_BLOCK.get(),cubeAll(Registration.OUTPUT_FLUID_BLOCK.get()));
    }
}
