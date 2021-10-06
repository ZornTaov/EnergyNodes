package org.zornco.energynodes.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.ChestLootTables;
import net.minecraft.loot.*;
import net.minecraft.util.ResourceLocation;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return ImmutableList.of(
                Pair.of(EnergyNodesChestLootTable::new, LootParameterSets.CHEST)
        );
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, @Nonnull ValidationTracker validationtracker) {
        map.forEach((resourceLocation, lootTable) -> LootTableManager.validate(validationtracker, resourceLocation, lootTable));
    }

    private static class EnergyNodesChestLootTable extends ChestLootTables {
        @Override
        public void accept(@Nonnull BiConsumer<ResourceLocation, LootTable.Builder> builder) {
            ImmutableList<ResourceLocation> list = ImmutableList.of(
                    LootTables.STRONGHOLD_CORRIDOR,
                    LootTables.BASTION_TREASURE,
                    LootTables.END_CITY_TREASURE,
                    LootTables.RUINED_PORTAL
            );
            createInjectPools(builder,
                    list,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantRange.exactly(1))
                                    .add(ItemLootEntry.lootTableItem(Registration.SAGE_MANIFEST_ITEM.get())
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
