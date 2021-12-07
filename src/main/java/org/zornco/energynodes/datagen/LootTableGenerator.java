package org.zornco.energynodes.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.ChestLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class LootTableGenerator extends LootTableProvider {

    public LootTableGenerator(DataGenerator generator) {
        super(generator);
    }

    @Nonnull
    @Override
    public String getName() {
        return EnergyNodes.MOD_ID + " Loot Tables";
    }

    @Nonnull
    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return ImmutableList.of(
                Pair.of(EnergyNodesChestLootTable::new, LootContextParamSets.CHEST)
        );
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, @Nonnull ValidationContext validationtracker) {
        map.forEach((resourceLocation, lootTable) -> LootTables.validate(validationtracker, resourceLocation, lootTable));
    }

    private static class EnergyNodesChestLootTable extends ChestLoot {
        @Override
        public void accept(@Nonnull BiConsumer<ResourceLocation, LootTable.Builder> builder) {
            ImmutableList<ResourceLocation> list = ImmutableList.of(
                    BuiltInLootTables.STRONGHOLD_CORRIDOR,
                    BuiltInLootTables.BASTION_TREASURE,
                    BuiltInLootTables.END_CITY_TREASURE,
                    BuiltInLootTables.RUINED_PORTAL
            );
            createInjectPools(builder,
                    list,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(Registration.SAGE_MANIFEST_ITEM.get())
                                            .setWeight(2)
                                    )
                            )
            );
        }

        public void createInjectPools(BiConsumer<ResourceLocation, LootTable.Builder> consumer, List<ResourceLocation> list, LootTable.Builder builder) {
            list.forEach(reLoc -> consumer.accept(new ResourceLocation(EnergyNodes.MOD_ID, "inject/chests/" + reLoc.getPath()), builder));
        }
    }
}
