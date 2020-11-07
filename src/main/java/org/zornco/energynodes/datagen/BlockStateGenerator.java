package org.zornco.energynodes.datagen;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;

public class BlockStateGenerator extends BlockStateProvider {
    public BlockStateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, EnergyNodes.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile controllerModel = models().orientable(Registration.ENERGY_CONTROLLER_BLOCK.get().getRegistryName().getPath(),
                new ResourceLocation(EnergyNodes.MOD_ID, "block/controller_side"),
                new ResourceLocation(EnergyNodes.MOD_ID, "block/controller_screen"),
                new ResourceLocation(EnergyNodes.MOD_ID, "block/controller_side")
        );
        horizontalBlock(Registration.ENERGY_CONTROLLER_BLOCK.get(), controllerModel);
        simpleBlockItem(Registration.ENERGY_CONTROLLER_BLOCK.get(), controllerModel);
        simpleBlock(Registration.ENERGY_INPUT_BLOCK.get());
        simpleBlockItem(Registration.ENERGY_INPUT_BLOCK.get(),cubeAll(Registration.ENERGY_INPUT_BLOCK.get()));
        simpleBlock(Registration.ENERGY_OUTPUT_BLOCK.get());
        simpleBlockItem(Registration.ENERGY_OUTPUT_BLOCK.get(),cubeAll(Registration.ENERGY_OUTPUT_BLOCK.get()));
    }
}
