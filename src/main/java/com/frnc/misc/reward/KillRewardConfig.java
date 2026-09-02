package com.frnc.misc.reward;

import com.frnc.misc.Misc;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 击杀掉落配置, 由数据包提供 (data/misc/kill_reward/rewards.json):
 *   掉落概率 = 怪物恶意等级 X * 每条配置的 chance Y (X 由 L2Hostility 提供)。
 * 数据由 {@link com.frnc.misc.MiscDataPackReloadListener} 在服务端数据包重载时 (含 /reload) 读取,
 * prepare 阶段读取 (loadFrom), apply 阶段写入内存 (apply)。
 */
public class KillRewardConfig
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String REWARDS_PATH = "kill_reward/rewards.json";

    private static List<KillRewardEntry> rewards = List.of();

    /** 从数据包资源管理器读取击杀掉落配置; 资源缺失或解析失败时保持空清单 */
    public static List<KillRewardEntry> loadFrom(ResourceManager manager)
    {
        List<KillRewardEntry> result = new ArrayList<>();
        try
        {
            Optional<Resource> resource = manager.getResource(ResourceLocation.fromNamespaceAndPath(Misc.MOD_ID, REWARDS_PATH));
            if (resource.isEmpty())
            {
                LOGGER.warn("[misc] 数据包资源 {}:{} 不存在, 击杀掉落为空", Misc.MOD_ID, REWARDS_PATH);
                return result;
            }
            try (var in = resource.get().open())
            {
                JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                JsonElement elem = root.get("rewards");
                if (elem != null && elem.isJsonArray())
                {
                    for (JsonElement e : (JsonArray) elem)
                    {
                        JsonObject obj = e.getAsJsonObject();
                        try
                        {
                            ResourceLocation id = ResourceLocation.tryParse(obj.get("item").getAsString());
                            if (id == null) continue;
                            double chance = obj.has("chance") ? obj.get("chance").getAsDouble() : 0;
                            int amount = obj.has("amount") ? obj.get("amount").getAsInt() : 1;
                            CompoundTag nbt = null;
                            if (obj.has("nbt"))
                            {
                                nbt = TagParser.parseTag(obj.get("nbt").getAsString());
                            }
                            result.add(new KillRewardEntry(id, Math.max(1, amount), Math.max(0, chance), nbt));
                        }
                        catch (CommandSyntaxException ex)
                        {
                            LOGGER.error("[misc] 击杀掉落配置条目 NBT 解析失败, 跳过该条: {}", ex.getMessage());
                        }
                    }
                }
            }
        }
        catch (IOException | RuntimeException ex)
        {
            // 读取失败时保持空清单, 不阻断游戏
            LOGGER.error("[misc] 读取击杀掉落配置失败: {}", ex.getMessage());
        }
        return result;
    }

    /** 将读取结果写入内存 (apply 阶段, 主线程) */
    public static void apply(List<KillRewardEntry> data)
    {
        rewards = data;
        LOGGER.info("[misc] 击杀掉落配置已加载: {} 条", rewards.size());
    }

    public static List<KillRewardEntry> getRewards()
    {
        return rewards;
    }
}
