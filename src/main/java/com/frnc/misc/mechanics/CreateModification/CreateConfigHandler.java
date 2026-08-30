package com.frnc.misc.mechanics.CreateModification;

import com.frnc.misc.Misc;
import com.mojang.logging.LogUtils;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;

/**
 * 修改机械动力 (Create) 锁链传动带最大连接长度配置:
 * 将 {@code AllConfigs.server().kinetics.maxChainConveyorLength} 默认值 32 -> 512 (方块)。
 *
 * Create 的配置是 catnip 的 {@code ConfigBase$CValue} 包装 (内部持有 ForgeConfigSpec.ConfigValue),
 * 通过其公开的 {@code set()} 覆写即可, 与 KaleidoscopeConfigHandler / ApotheoticL2HostilityConfigHandler 同套路。
 *
 * applyOverrides() 在 Misc.commonSetup 中调用 (所有模组配置加载完成后执行, 保证可靠生效);
 * ModConfigEvent 处理器用于配置重载时再次应用 (否则重载会回落到配置文件里的原值)。
 */
public class CreateConfigHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();

    /** 目标默认值 (方块数) */
    private static final int MAX_CHAIN_CONVEYOR_LENGTH = 512;

    /** 覆盖 Create maxChainConveyorLength 配置 (须在所有配置加载完成后调用) */
    public static void applyOverrides()
    {
        AllConfigs.server().kinetics.maxChainConveyorLength.set(MAX_CHAIN_CONVEYOR_LENGTH);
        LOGGER.info("[misc] Create maxChainConveyorLength override -> {}",
                AllConfigs.server().kinetics.maxChainConveyorLength.get());
    }

    @Mod.EventBusSubscriber(modid = Misc.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class EventHandler
    {
        /** 配置加载/重载时再次应用覆盖 (ModConfigEvent 同时覆盖 Loading / Reloading) */
        @SubscribeEvent
        public static void onConfigLoad(final ModConfigEvent event)
        {
            if ("create".equals(event.getConfig().getModId()))
            {
                applyOverrides();
            }
        }
    }
}
