package org.zornco.energynodes.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;
import org.zornco.energynodes.EnergyNodes;

import static org.zornco.energynodes.Registration.*;

public class LangGenerator extends LanguageProvider {
    public LangGenerator(DataGenerator generator) {
        super(generator, EnergyNodes.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup."+EnergyNodes.MOD_ID, "Energy Nodes"); // "itemGroup.energynodes"
        add(TEST_PAD_ITEM.get() , "Test Pad"); // "item.energynodes.test_pad"
        add(ENERGY_LINKER_ITEM.get(), "Energy Linker"); // "item.energynodes.energy_linker"
        TIER_UPGRADES_MAP.forEach((name, tier) -> add(tier.get(), toProperCase(name) + " Tier Upgrade")); // "item.energynodes.tier_upgrade_advanced"
        add(SAGE_MANIFEST_ITEM.get(), "Sage Manifest"); // "item.energynodes.sage_manifest"
        add(ENERGY_CONTROLLER_BLOCK.get(), "Energy Controller"); // "block.energynodes.energy_controller"
        add(INPUT_NODE_BLOCK.get(), "Input Node"); // "block.energynodes.input_node"
        add(OUTPUT_NODE_BLOCK.get(), "Output Node"); // "block.energynodes.output_node"
        addTer("base", "Base"); // "energynodes.ter.base"
        addTer("advanced", "Advanced"); // "energynodes.ter.advanced"
        addTer("expert", "Expert"); // "energynodes.ter.expert"
        addTer("max", "Max"); // "energynodes.ter.max"
        addTop("connected_to", "Connected To: %s"); // "energynodes.top.connected_to"
        addTop("transferred", "FE/t: %s"); // "energynodes.top.transferred"
        addLinker("connected_to", "Connected To: %s"); // "energynodes.linker.connected_to"
        addLinker("disconnected", "Disconnected from: %s"); // "energynodes.linker.disconnected"
        addLinker("too_many_connections", "Controller has too many connections, limit: %s"); // "energynodes.linker.too_many_connections"
        addLinker("start_connection", "Starting connection from: %s"); // "energynodes.linker.start_connection"
        addLinker("controller_missing", "Controller has no Tile?!"); // "energynodes.linker.controller_missing"
        addLinker("node_missing", "Node missing at: %s"); // "energynodes.linker.node_missing"
        addLinker("node_out_of_range", "Node out of range, limit: %s"); // "energynodes.linker.node_out_of_range"
    }
    public static String toProperCase(final String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
    /**
     * Add a Tile Entity Rendered string
     */
    private void addTer(String key, String value) {
        add(key, "ter", value);
    }

    /**
     * Add a "The One Probe" info string
     */
    private void addTop(String key, String value) {
        add(key, "top", value);
    }

    /**
     * Add a string for the Linker
     */
    private void addLinker(String key, String value) {
        add(key, "linker", value);
    }

    private void add(String key, String type, String value) {
        add(String.format("%s.%s.%s", EnergyNodes.MOD_ID, type, key), value);
    }
}
