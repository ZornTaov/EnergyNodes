package org.zornco.energynodes.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;

public interface NodeBlockTags {
    TagKey<Block> CONTROLLER_TAG = TagKey.create(Registration.BLOCKS.getRegistryKey(),
        new ResourceLocation(EnergyNodes.MOD_ID, "controller"));
    TagKey<Block> NODE_TAG = TagKey.create(Registration.BLOCKS.getRegistryKey(),
        new ResourceLocation(EnergyNodes.MOD_ID, "node"));

    TagKey<Block> ENERGY_TAG = TagKey.create(Registration.BLOCKS.getRegistryKey(),
        new ResourceLocation(EnergyNodes.MOD_ID, "energy"));
    TagKey<Block> FLUID_TAG = TagKey.create(Registration.BLOCKS.getRegistryKey(),
        new ResourceLocation(EnergyNodes.MOD_ID, "fluid"));

}