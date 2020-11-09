package org.zornco.energynodes;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.zornco.energynodes.block.EnergyControllerBlock;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.item.EnergyLinkerItem;
import org.zornco.energynodes.item.TestPadItem;
import org.zornco.energynodes.tile.EnergyControllerTile;
import org.zornco.energynodes.tile.EnergyNodeTile;

import static org.zornco.energynodes.block.EnergyNodeBlock.*;

@Mod.EventBusSubscriber(modid = EnergyNodes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registration {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, EnergyNodes.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EnergyNodes.MOD_ID);
    private static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, EnergyNodes.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, EnergyNodes.MOD_ID);

    // ================================================================================================================
    //   PROPERTIES
    // ================================================================================================================
    private static final AbstractBlock.Properties baseProperty = Block.Properties
            .create(Material.IRON)
            .hardnessAndResistance(3.0f, 128.0f)
            .harvestTool(ToolType.PICKAXE);

    // ================================================================================================================
    //    ITEMS
    // ================================================================================================================
    public static final RegistryObject<TestPadItem> TEST_PAD_ITEM = ITEMS.register("test_pad", TestPadItem::new);
    public static final RegistryObject<EnergyLinkerItem> ENERGY_LINKER_ITEM = ITEMS.register("energy_linker", EnergyLinkerItem::new);

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
    public static final RegistryObject<Item> ENERGY_CONTROLLER_ITEM =
            ITEMS.register("energy_controller", () ->
                    new BlockItem(ENERGY_CONTROLLER_BLOCK.get(),
                            new Item.Properties().group(Registration.ITEM_GROUP))
            );
    public static final RegistryObject<Item> INPUT_NODE_ITEM =
            ITEMS.register("input_node", () ->
                    new BlockItem(INPUT_NODE_BLOCK.get(),
                            new Item.Properties().group(Registration.ITEM_GROUP))
            );
    public static final RegistryObject<Item> OUTPUT_NODE_ITEM =
            ITEMS.register("output_node", () ->
                    new BlockItem(OUTPUT_NODE_BLOCK.get(),
                            new Item.Properties().group(Registration.ITEM_GROUP))
            );

    // ================================================================================================================
    //    TILE ENTITIES
    // ================================================================================================================
    public static final RegistryObject<TileEntityType<EnergyControllerTile>> ENERGY_CONTROLLER_TILE =
            TILES.register("energy_controller", () ->
                    TileEntityType.Builder.create(EnergyControllerTile::new, ENERGY_CONTROLLER_BLOCK.get()
                    ).build(null));
    public static final RegistryObject<TileEntityType<EnergyNodeTile>> ENERGY_TRANSFER_TILE =
            TILES.register("energy_transfer", () ->
                    TileEntityType.Builder.create(EnergyNodeTile::new, INPUT_NODE_BLOCK.get(), OUTPUT_NODE_BLOCK.get()
                    ).build(null));

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TILES.register(modEventBus);
        ENTITIES.register(modEventBus);
    }
    public static final ItemGroup ITEM_GROUP = new ItemGroup(EnergyNodes.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Registration.TEST_PAD_ITEM.get());
        }
    };
}