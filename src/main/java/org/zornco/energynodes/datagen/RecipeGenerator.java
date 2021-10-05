package org.zornco.energynodes.datagen;

import net.minecraft.block.Blocks;
import net.minecraft.data.*;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;
import org.zornco.energynodes.Registration;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void buildShapelessRecipes(@Nonnull Consumer<IFinishedRecipe> consumer) {

        // ================================================================================================================
        //    BLOCKS
        // ================================================================================================================
        ShapedRecipeBuilder.shaped(Registration.ENERGY_CONTROLLER_ITEM.get(), 1)
                .pattern("SSS")
                .pattern("HGH")
                .pattern("SCS")
                .define('G', Tags.Items.GLASS_PANES)
                .define('C', Items.COMPARATOR)
                .define('H', Items.TRIPWIRE_HOOK)
                .define('S', Tags.Items.STONE)
                .unlockedBy("has_redstone_torch", has(Blocks.REDSTONE_TORCH))
                .save(consumer);
        ShapedRecipeBuilder.shaped(Registration.INPUT_NODE_ITEM.get(), 1)
                .pattern("SSS")
                .pattern("C s")
                .pattern("SSS")
                .define('C', Items.COMPARATOR)
                .define('S', Tags.Items.INGOTS_IRON)
                .define('s', Tags.Items.STRING)
                .unlockedBy("has_redstone_torch", has(Blocks.REDSTONE_TORCH))
                .save(consumer);
        ShapedRecipeBuilder.shaped(Registration.OUTPUT_NODE_ITEM.get(), 1)
                .pattern("SSS")
                .pattern("s R")
                .pattern("SSS")
                .define('R', Items.REPEATER)
                .define('S', Tags.Items.INGOTS_IRON)
                .define('s', Tags.Items.STRING)
                .unlockedBy("has_redstone_torch", has(Blocks.REDSTONE_TORCH))
                .save(consumer);

        // ================================================================================================================
        //    ITEMS
        // ================================================================================================================
        ShapedRecipeBuilder.shaped(Registration.ENERGY_LINKER_ITEM.get(), 1)
                .pattern(" R ")
                .pattern("SGS")
                .pattern("SBS")
                .define('R', Items.REDSTONE_TORCH)
                .define('G', Tags.Items.GLASS_PANES)
                .define('S', Tags.Items.INGOTS_IRON)
                .define('B', Items.STONE_BUTTON)
                .unlockedBy("has_redstone_torch", has(Blocks.REDSTONE_TORCH))
                .save(consumer);
    }
}
