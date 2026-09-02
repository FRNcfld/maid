package com.frnc.misc.reward;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * 一条击杀掉落配置 (对应数据包 data/misc/kill_reward/rewards.json 中 rewards 数组的一条)。
 *
 * @param itemId 物品 ID, 如 "minecraft:diamond"
 * @param amount 掉落数量 (默认 1)
 * @param chance 概率系数 Y; 实际掉落概率 = 怪物恶意等级 X * Y
 * @param nbt    物品附加 NBT (SNBT 解析结果, 可为 null), 用于附魔书等需要附加数据的物品
 */
public record KillRewardEntry(ResourceLocation itemId, int amount, double chance, @Nullable CompoundTag nbt)
{
}
