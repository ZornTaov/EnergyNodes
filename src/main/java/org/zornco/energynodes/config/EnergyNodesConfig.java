package org.zornco.energynodes.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//@Mod.EventBusSubscriber
public class EnergyNodesConfig {

    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_TIERS = "tiers";

    public static ForgeConfigSpec SERVER_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    private static ArrayList<String> tierNameList;
    private static ArrayList<Integer> maxConnectionsList;
    private static ArrayList<Integer> maxTransferSpeedList;
    private static ArrayList<Integer> maxRadiusList;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> tierNameConfig;
    private static ForgeConfigSpec.ConfigValue<List<? extends Integer>> maxConnectionsConfig;
    private static ForgeConfigSpec.ConfigValue<List<? extends Integer>> maxTransferSpeedConfig;
    private static ForgeConfigSpec.ConfigValue<List<? extends Integer>> maxConnectionRadiusConfig;


    public static void setupConfigs() {

        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

        //CLIENT_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        //ROTATION_SPEED = CLIENT_BUILDER.comment("Rotation speed of the magic block").defineInRange("rotationSpeed", 100.0, 0.0, 1000000.0);
        //CLIENT_BUILDER.pop();


        setupTiers(SERVER_BUILDER);



        SERVER_CONFIG = SERVER_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    private static void setupTiers(ForgeConfigSpec.Builder SERVER_BUILDER) {
        SERVER_BUILDER.comment("Tier settings").push(CATEGORY_TIERS);

        SERVER_BUILDER.comment("EACH CONFIG MUST HAVE THE SAME NUMBER OF ELEMENTS.");

        SERVER_BUILDER.comment("Tier names. Must have at least one entry.");
        tierNameConfig = SERVER_BUILDER
                .defineList("TierNames",
                        Arrays.asList("base", "advanced", "expert", "max"),
                        obj -> obj instanceof String);

        SERVER_BUILDER.comment("Max Number of Connections. Must have at least one number, Min: 2 Max: 100");
        maxConnectionsConfig = SERVER_BUILDER
                .defineList("MaxConnections",
                        Arrays.asList(2, 4, 8, 16),
                        EnergyNodesConfig::intArrayTest);

        SERVER_BUILDER.comment("Max Number of Connections. Must have at least one number, Min: 2 Max: 100");
        maxTransferSpeedConfig = SERVER_BUILDER
                .defineList("MaxTransferSpeed",
                        Arrays.asList(200, 2000, 200000, -1),
                        EnergyNodesConfig::intArrayTest);

        SERVER_BUILDER.comment("Max Number of Connections. Must have at least one number, Min: 2 Max: 100");
        maxConnectionRadiusConfig = SERVER_BUILDER
                .defineList("MaxConnectionRadius",
                        Arrays.asList(2, 4, 16, 128),
                        EnergyNodesConfig::intArrayTest);

        SERVER_BUILDER.pop();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {

        /*tierNameList = new ArrayList<>(tierNameConfig.get());
        maxConnectionsList = new ArrayList<>(maxConnectionsConfig.get());
        maxTransferSpeedList = new ArrayList<>(maxTransferSpeedConfig.get());
        maxRadiusList = new ArrayList<>(maxConnectionRadiusConfig.get());*/
    }

    @SubscribeEvent
    public static void onReload(final ModConfig.Reloading configEvent) {
        /*tierNameList = new ArrayList<>(tierNameConfig.get());
        maxConnectionsList = new ArrayList<>(maxConnectionsConfig.get());
        maxTransferSpeedList = new ArrayList<>(maxTransferSpeedConfig.get());
        maxRadiusList = new ArrayList<>(maxConnectionRadiusConfig.get());*/
    }

    private static boolean intArrayTest(Object obj) {
        if (obj instanceof Integer) {
            try {
                int integer = Integer.decode(String.valueOf(obj));

                return integer >= 2 || integer == -1;
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    public static ArrayList<String> getTierNameList() {
        return tierNameList;
    }

    public static ArrayList<Integer> getMaxConnectionsList() {
        return maxConnectionsList;
    }

    public static ArrayList<Integer> getMaxTransferSpeedList() {
        return maxTransferSpeedList;
    }

    public static ArrayList<Integer> getMaxRadiusList() {
        return maxRadiusList;
    }
}
