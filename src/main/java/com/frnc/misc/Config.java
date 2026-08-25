package com.frnc.misc;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Misc.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue DOUBLE_JUMP_ENABLED = BUILDER
            .comment("Whether double jump is enabled by default (can be toggled in-game with the J key)")
            .define("doubleJumpEnabled", true);

    private static final ForgeConfigSpec.BooleanValue CLEANUP_ENABLED = BUILDER
            .comment("Whether to periodically clean up dropped items")
            .define("cleanupEnabled", true);

    private static final ForgeConfigSpec.IntValue CLEANUP_INTERVAL_SECONDS = BUILDER
            .comment("Seconds between automatic dropped-item cleanups")
            .defineInRange("cleanupIntervalSeconds", 600, 10, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.BooleanValue CLEANUP_PROTECT_NAMED_ITEMS = BUILDER
            .comment("Whether dropped items with a custom name are protected from cleanup")
            .define("cleanupProtectNamedItems", true);

    private static final ForgeConfigSpec.IntValue CLEANUP_MIN_ITEM_AGE_SECONDS = BUILDER
            .comment("Minimum age (seconds) of an item before it can be cleaned, protecting fresh drops")
            .defineInRange("cleanupMinimumItemAgeSeconds", 10, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CLEANUP_MAX_ITEMS_PER_BATCH = BUILDER
            .comment("Maximum items removed per tick during cleanup (to avoid lag)")
            .defineInRange("cleanupMaxItemsPerBatch", 500, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.BooleanValue CLEANUP_BROADCAST_RESULT = BUILDER
            .comment("Whether to broadcast a chat message after each cleanup")
            .define("cleanupBroadcastResult", true);

    private static final ForgeConfigSpec.IntValue CLEANUP_WARNING_SECONDS = BUILDER
            .comment("Broadcast a warning this many seconds before each cleanup (0 = disabled)")
            .defineInRange("cleanupWarningSeconds", 10, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.BooleanValue CLEANUP_ITEM_BLACKLIST_ENABLE = BUILDER
            .comment("Whether the item blacklist applies (blacklist = must clean; see misc-blacklist.json)")
            .define("cleanupItemBlacklistEnable", false);

    private static final ForgeConfigSpec.BooleanValue CLEANUP_ITEM_WHITELIST_ENABLE = BUILDER
            .comment("Whether the item whitelist applies (whitelist = protected; see misc-whitelist.json)")
            .define("cleanupItemWhitelistEnable", false);

    private static final ForgeConfigSpec.BooleanValue CLEANUP_DIMENSION_BLACKLIST_ENABLE = BUILDER
            .comment("Whether the dimension blacklist applies (blacklist = clean)")
            .define("cleanupDimensionBlacklistEnable", false);

    private static final ForgeConfigSpec.BooleanValue CLEANUP_DIMENSION_WHITELIST_ENABLE = BUILDER
            .comment("Whether the dimension whitelist applies (whitelist = skip)")
            .define("cleanupDimensionWhitelistEnable", false);

    private static final ForgeConfigSpec.BooleanValue CLEANUP_PROTECT_PLAYER_DEATH_DROPS = BUILDER
            .comment("Whether items dropped on player death are protected from cleanup")
            .define("cleanupProtectPlayerDeathDrops", true);

    private static final ForgeConfigSpec.IntValue CLEANUP_DEATH_DROP_PROTECTION_SECONDS = BUILDER
            .comment("How many seconds player death drops are protected from cleanup")
            .defineInRange("cleanupPlayerDeathDropProtectionSeconds", 30, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.BooleanValue APOTHESIS_L2HOSTILITY_FIX_ENABLED = BUILDER
            .comment("Master switch for the Apotheosis + L2Hostility integration: makes mobs spawned by Apotheosis spawners (including chorus-fruit No-AI spawners) receive L2Hostility levels and affixes")
            .define("apotheosisL2HostilityFixEnabled", true);

    private static final ForgeConfigSpec.IntValue CURSE_OF_TIMID_DROP_LEVEL = BUILDER
            .comment("Minimum L2 Hostility difficulty level of a monster: the first time a player kills such a monster, the Curse of Timid (misc:curse_of_timid) drops")
            .defineInRange("curseOfTimidDropLevel", 1000, 1, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean doubleJumpEnabled;
    public static boolean cleanupEnabled;
    public static int cleanupIntervalSeconds;
    public static boolean cleanupProtectNamedItems;
    public static int cleanupMinimumItemAgeSeconds;
    public static int cleanupMaxItemsPerBatch;
    public static boolean cleanupBroadcastResult;
    public static int cleanupWarningSeconds;
    public static boolean cleanupItemBlacklistEnable;
    public static boolean cleanupItemWhitelistEnable;
    public static boolean cleanupDimensionBlacklistEnable;
    public static boolean cleanupDimensionWhitelistEnable;
    public static boolean cleanupProtectPlayerDeathDrops;
    public static int cleanupPlayerDeathDropProtectionSeconds;
    public static boolean apotheosisL2HostilityFixEnabled;
    public static int curseOfTimidDropLevel;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        doubleJumpEnabled = DOUBLE_JUMP_ENABLED.get();
        cleanupEnabled = CLEANUP_ENABLED.get();
        cleanupIntervalSeconds = CLEANUP_INTERVAL_SECONDS.get();
        cleanupProtectNamedItems = CLEANUP_PROTECT_NAMED_ITEMS.get();
        cleanupMinimumItemAgeSeconds = CLEANUP_MIN_ITEM_AGE_SECONDS.get();
        cleanupMaxItemsPerBatch = CLEANUP_MAX_ITEMS_PER_BATCH.get();
        cleanupBroadcastResult = CLEANUP_BROADCAST_RESULT.get();
        cleanupWarningSeconds = CLEANUP_WARNING_SECONDS.get();
        cleanupItemBlacklistEnable = CLEANUP_ITEM_BLACKLIST_ENABLE.get();
        cleanupItemWhitelistEnable = CLEANUP_ITEM_WHITELIST_ENABLE.get();
        cleanupDimensionBlacklistEnable = CLEANUP_DIMENSION_BLACKLIST_ENABLE.get();
        cleanupDimensionWhitelistEnable = CLEANUP_DIMENSION_WHITELIST_ENABLE.get();
        cleanupProtectPlayerDeathDrops = CLEANUP_PROTECT_PLAYER_DEATH_DROPS.get();
        cleanupPlayerDeathDropProtectionSeconds = CLEANUP_DEATH_DROP_PROTECTION_SECONDS.get();
        apotheosisL2HostilityFixEnabled = APOTHESIS_L2HOSTILITY_FIX_ENABLED.get();
        curseOfTimidDropLevel = CURSE_OF_TIMID_DROP_LEVEL.get();
    }
}
