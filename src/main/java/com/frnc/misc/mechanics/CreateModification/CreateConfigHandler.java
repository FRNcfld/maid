package com.frnc.misc.mechanics.CreateModification;

import com.frnc.misc.Misc;
import com.mojang.logging.LogUtils;
import com.simibubi.create.infrastructure.config.AllConfigs;
import org.slf4j.Logger;

/**
 * 修改机械动力 (Create) 锁链传动带最大连接长度配置:
 * 将 {@code AllConfigs.server().kinetics.maxChainConveyorLength} 默认值 32 -> 512 (方块)。
 *
 * Create 的配置是 catnip 的 {@code ConfigBase$CValue} 包装 (内部持有 ForgeConfigSpec.ConfigValue),
 * 通过其公开的 {@code set()} 覆写即可。
 *
 * 时序 (重要): 该值位于 Create 的 SERVER 类型配置 (create-server.toml, 按存档存于
 * saves/&lt;world&gt;/serverconfig/), Forge 的 SERVER 配置只在进档/开服时加载
 * (ConfigTracker.loadDefaultServerConfigs)。因此:
 * <ul>
 *   <li>不能在 commonSetup 中调用: 模组加载阶段该 ConfigValue 尚未绑定 config 对象,
 *       set() 会抛 "Cannot set config value without assigned Config object present" (NPE)。</li>
 *   <li>ModConfigEvent 也收不到: Forge 把 ModConfigEvent 分发到配置所属 mod 自己的事件总线
 *       (ModContainer.dispatchConfigEvent), 不是 misc 的总线。</li>
 *   <li>可靠挂钩点是 Misc.onServerStarting (Forge 总线, 每次进服/开服触发, 且晚于配置加载)。</li>
 * </ul>
 *
 * 已知限制: 若运行中 create-server.toml 被改动并触发配置重载, 值会回落到文件值, 下次进服恢复 512。
 */
public class CreateConfigHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();

    /** 目标默认值 (方块数) */
    private static final int MAX_CHAIN_CONVEYOR_LENGTH = 512;

    /** 覆盖 Create maxChainConveyorLength 配置 (须在 create SERVER 配置加载后调用) */
    public static void applyOverrides()
    {
        AllConfigs.server().kinetics.maxChainConveyorLength.set(MAX_CHAIN_CONVEYOR_LENGTH);
        LOGGER.info("[misc] Create maxChainConveyorLength override -> {}",
                AllConfigs.server().kinetics.maxChainConveyorLength.get());
    }
}
