package org.zornco.energynodes;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryBuilder;
import org.zornco.energynodes.block.EnergyControllerBlock;
import org.zornco.energynodes.block.EnergyNodeBlock;
//import org.zornco.energynodes.config.EnergyNodesConfig;
import org.zornco.energynodes.item.EnergyLinkerItem;
import org.zornco.energynodes.item.SageManifestItem;
import org.zornco.energynodes.item.TestPadItem;
import org.zornco.energynodes.item.TierUpgradeItem;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.tiers.ControllerTier;
import org.zornco.energynodes.tiers.IControllerTier;
import org.zornco.energynodes.tile.EnergyControllerTile;
import org.zornco.energynodes.tile.EnergyNodeTile;
import org.zornco.energynodes.block.EnergyNodeBlock.Flow;

import javax.annotation.Nonnull;
import java.util.*;

public class Registration {
    // ================================================================================================================
    //    Registries
    // ================================================================================================================
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, EnergyNodes.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EnergyNodes.MOD_ID);
    private static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, EnergyNodes.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, EnergyNodes.MOD_ID);
    public static final DeferredRegister<ControllerTier> TIERS = DeferredRegister.create(ControllerTier.class, EnergyNodes.MOD_ID);
    public static final RegistryObject<ControllerTier> BASE;
    static {
        TIERS.makeRegistry("tiers",
                () -> new RegistryBuilder<ControllerTier>()
                        .setType(ControllerTier.class)
                        .tagFolder("tiers"));
    }
    // ================================================================================================================
    //   PROPERTIES
    // ================================================================================================================
    private static final AbstractBlock.Properties baseProperty = Block.Properties
            .of(Material.METAL)
            .strength(3.0f, 128.0f)
            .harvestTool(ToolType.PICKAXE);

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
    public static final RegistryObject<TileEntityType<EnergyControllerTile>> ENERGY_CONTROLLER_TILE =
            TILES.register("energy_controller", () ->
                    TileEntityType.Builder.of(EnergyControllerTile::new, ENERGY_CONTROLLER_BLOCK.get()
                    ).build(null));
    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<TileEntityType<EnergyNodeTile>> ENERGY_TRANSFER_TILE =
            TILES.register("energy_transfer", () ->
                    TileEntityType.Builder.of(EnergyNodeTile::new, INPUT_NODE_BLOCK.get(), OUTPUT_NODE_BLOCK.get()
                    ).build(null));

    // ================================================================================================================
    //    CAPABILITIES
    // ================================================================================================================
    @SuppressWarnings("CanBeFinal")
    @CapabilityInject(IControllerTier.class)
    public static Capability<IControllerTier> TIER_CAPABILITY = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IControllerTier.class, new Capability.IStorage<IControllerTier>()
            {
                @Override
                public INBT writeNBT(Capability<IControllerTier> capability, IControllerTier instance, Direction side)
                {
                    return StringNBT.valueOf(instance.getSerializedName());
                }

                @Override
                public void readNBT(Capability<IControllerTier> capability, IControllerTier instance, Direction side, INBT nbt)
                {
                    if (!(instance instanceof ControllerTier))
                        throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                    instance.setTier(ControllerTier.getTierFromString(nbt.getAsString()));
                }
            },
            ControllerTier::new);
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
        NetworkManager.Register();
    }
    public static final ItemGroup ITEM_GROUP = new ItemGroup(EnergyNodes.MOD_ID) {
        @Nonnull
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.TEST_PAD_ITEM.get());
        }
    };
}