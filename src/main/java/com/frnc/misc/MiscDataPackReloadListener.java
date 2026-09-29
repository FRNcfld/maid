package com.frnc.misc;

import com.frnc.misc.reward.KillRewardConfig;
import com.frnc.misc.reward.KillRewardEntry;
import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.List;

/**
 * 服务端数据包重载时读取本模组的数据包配置文件并写入内存。
 * 通过 AddReloadListenerEvent 注册: 服务端启动与世界 /reload 时都会执行。
 * 目前加载:
 *   - 击杀掉落配置 (data/misc/kill_reward/rewards.json)
 * prepare 阶段在后台线程解析, apply 阶段在主线程提交, 保证静态数据的内存可见性。
 *
 * <p>掉落物清理黑白名单已随清理功能迁往 {@code better_experience}
 * （见该模组的 BetterExperienceDataPackReloadListener）。
 */
@Mod.EventBusSubscriber(modid = Misc.MOD_ID)
public class MiscDataPackReloadListener
{
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onAddReloadListener(final AddReloadListenerEvent event)
    {
        event.addListener(new SimplePreparableReloadListener<List<KillRewardEntry>>()
        {
            @Override
            protected List<KillRewardEntry> prepare(ResourceManager resourceManager, ProfilerFiller profiler)
            {
                return KillRewardConfig.loadFrom(resourceManager);
            }

            @Override
            protected void apply(List<KillRewardEntry> data, ResourceManager resourceManager, ProfilerFiller profiler)
            {
                KillRewardConfig.apply(data);
                LOGGER.info("[misc] 数据包配置已加载: 击杀掉落 {} 条", data.size());
            }
        });
    }
}
