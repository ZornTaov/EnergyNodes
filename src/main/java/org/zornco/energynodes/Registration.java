package org.zornco.energynodes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;
import org.zornco.energynodes.block.EnergyControllerBlock;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.item.EnergyLinkerItem;
import org.zornco.energynodes.item.SageManifestItem;
import org.zornco.energynodes.item.TestPadItem;
import org.zornco.energynodes.item.TierUpgradeItem;
import org.zornco.energynodes.tiers.ControllerTier;
import org.zornco.energynodes.tiers.IControllerTier;
import org.zornco.energynodes.tile.EnergyControllerTile;
import org.zornco.energynodes.tile.EnergyNodeTile;
import org.zornco.energynodes.block.EnergyNodeBlock.Flow;

import javax.annotation.Nonnull;
import java.util.*;

@Mod.EventBusSubscriber(modid = EnergyNodes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registration {
    // ================================================================================================================
    //    Registries
    // ================================================================================================================
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, EnergyNodes.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EnergyNodes.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, EnergyNodes.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, EnergyNodes.MOD_ID);
    public static final ResourceLocation TierRec = new ResourceLocation(EnergyNodes.MOD_ID, "tiers");
    public static final DeferredRegister<ControllerTier> TIERS = DeferredRegister.create(TierRec, EnergyNodes.MOD_ID);
    public static final RegistryObject<ControllerTier> BASE;

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void newRegistries(final NewRegistryEvent evt) {
        final var b = new RegistryBuilder<ControllerTier>()
            .setName(TierRec)
            .setType(ControllerTier.class);

        evt.create(b);
    }

    // ================================================================================================================
    //   PROPERTIES
    // ================================================================================================================
    private static final BlockBehaviour.Properties baseProperty = Block.Properties
            .of(Material.METAL)
            .strength(3.0f, 128.0f);
            //.harvestTool(ToolType.PICKAXE);

    // ================================================================================================================
    //    ITEMS
    // ================================================================================================================
    // TODO - remove for final build?
    public static final RegistryObject<TestPadItem> TEST_PAD_ITEM = ITEMS.register("test_pad", TestPadItem::new);
    public static final RegistryObject<EnergyLinkerItem> ENERGY_LINKER_ITEM = ITEMS.register("energy_linker", EnergyLinkerItem::new);

    public static final HashMap<String, RegistryObject<TierUpgradeItem>> TIER_UPGRADES_MAP = new HashMap<>();
    public static final HashMap<String, RegistryObject<ControllerTier>> TIER_MAP = new HashMap<>();

    static {
        BASE = RegisterTier("base", 0, 400, 2, 16, false);
        RegisterTier("advanced", 25, 40000, 4, 32);
        RegisterTier("expert", 50, 4000000, 8, 64);
        RegisterTier("max", 100, EnergyNodeConstants.UNLIMITED_RATE, 16, 128);
        /*BASE = RegisterTier(EnergyNodesConfig.getTierNameList().get(0),
                0,
                EnergyNodesConfig.getMaxTransferSpeedList().get(0),
                EnergyNodesConfig.getMaxConnectionsList().get(0),
                EnergyNodesConfig.getMaxRadiusList().get(0), false);
        for (int level = 1; level < EnergyNodesConfig.getTierNameList().size(); level++)
            RegisterTier(EnergyNodesConfig.getTierNameList().get(level),
                    level,
                    EnergyNodesConfig.getMaxTransferSpeedList().get(level),
                    EnergyNodesConfig.getMaxConnectionsList().get(level),
                    EnergyNodesConfig.getMaxRadiusList().get(level));*/
    }

    private static void RegisterTier(String name, int level, int maxTransfer, int maxConnections, int maxRange) {
        RegisterTier(name, level, maxTransfer, maxConnections, maxRange, true);
    }

    private static RegistryObject<ControllerTier> RegisterTier(String name, int level, int maxTransfer, int maxConnections, int maxRange, boolean genItem) {
        ControllerTier tier = new ControllerTier(name, level, maxTransfer, maxConnections, maxRange);
        RegistryObject<ControllerTier> tierRO = TIERS.register("tier_" + name, () -> tier);
        TIER_MAP.put(name, tierRO);
        if(genItem)
            TIER_UPGRADES_MAP.put(name, ITEMS.register("tier_upgrade_" + name, () -> new TierUpgradeItem(tier)));
        return tierRO;
    }


    // ================================================================================================================
    //    BLOCKS
    // ================================================================================================================
    public static final RegistryObject<EnergyControllerBlock> ENERGY_CONTROLLER_BLOCK =
            BLOCKS.register("energy_controller", () -> new EnergyControllerBlock(baseProperty));
    public static final RegistryObject<EnergyNodeBlock> INPUT_NODE_BLOCK =
            BLOCKS.register("input_node", () -> new EnergyNodeBlock(baseProperty, Flow.IN));
    public static final RegistryObject<EnergyNodeBlock> OUTPUT_NODE_BLOCK =
            BLOCKS.register("output_node", () -> new EnergyNodeBlock(baseProperty, Flow.OUT));

    // ================================================================================================================
    //    ITEM BLOCKS
    // ================================================================================================================
    @SuppressWarnings("unused")
    public static final RegistryObject<Item> ENERGY_CONTROLLER_ITEM =
            ITEMS.register("energy_controller", () ->
                    new BlockItem(ENERGY_CONTROLLER_BLOCK.get(),
                            new Item.Properties().tab(Registration.ITEM_GROUP))
            );
    @SuppressWarnings("unused")
    public static final RegistryObject<Item> INPUT_NODE_ITEM =
            ITEMS.register("input_node", () ->
                    new BlockItem(INPUT_NODE_BLOCK.get(),
                            new Item.Properties().tab(Registration.ITEM_GROUP))
            );
    @SuppressWarnings("unused")
    public static final RegistryObject<Item> OUTPUT_NODE_ITEM =
            ITEMS.register("output_node", () ->
                    new BlockItem(OUTPUT_NODE_BLOCK.get(),
                            new Item.Properties().tab(Registration.ITEM_GROUP))
            );

    // ================================================================================================================
    //    TILE ENTITIES
    // ================================================================================================================
    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<EnergyControllerTile>> ENERGY_CONTROLLER_TILE =
            TILES.register("energy_controller", () ->
                    BlockEntityType.Builder.of(EnergyControllerTile::new, ENERGY_CONTROLLER_BLOCK.get()
                    ).build(null));
    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<EnergyNodeTile>> ENERGY_TRANSFER_TILE =
            TILES.register("energy_transfer", () ->
                    BlockEntityType.Builder.of(EnergyNodeTile::new, INPUT_NODE_BLOCK.get(), OUTPUT_NODE_BLOCK.get()
                    ).build(null));

    // ================================================================================================================
    //    CAPABILITIES
    // ================================================================================================================

    public static Capability<IControllerTier> TIER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent evt)
    {
        evt.register(IControllerTier.class);
    }

    // ================================================================================================================
    //    EASTER EGGS
    // ================================================================================================================
    public static final RegistryObject<SageManifestItem> SAGE_MANIFEST_ITEM = ITEMS.register("sage_manifest", SageManifestItem::new);


    public static void init(IEventBus modEventBus) {
        TIERS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TILES.register(modEventBus);
        ENTITIES.register(modEventBus);
        ClientRegistration.PARTICLE.register(modEventBus);
    }
    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(EnergyNodes.MOD_ID) {
        @Nonnull
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.TEST_PAD_ITEM.get());
        }
    };
}