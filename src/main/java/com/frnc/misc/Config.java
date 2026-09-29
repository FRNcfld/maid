package com.frnc.misc;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * 模组配置。走 Forge 标准配置系统，生成在 {@code config/misc-common.toml}。
 *
 * <p>二段跳 / 鞘翅飞行开关 / 掉落物清理 / 附魔金苹果强化已拆分为独立模组
 * {@code better_experience}，对应配置项一并迁出；这里只剩神化 × L2Hostility 的总开关。
 */
@Mod.EventBusSubscriber(modid = Misc.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue APOTHESIS_L2HOSTILITY_FIX_ENABLED = BUILDER
            .comment("Master switch for the Apotheosis + L2Hostility integration: makes mobs spawned by Apotheosis spawners (including chorus-fruit No-AI spawners) receive L2Hostility levels and affixes")
            .define("apotheosisL2HostilityFixEnabled", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean apotheosisL2HostilityFixEnabled;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        apotheosisL2HostilityFixEnabled = APOTHESIS_L2HOSTILITY_FIX_ENABLED.get();
    }
}
