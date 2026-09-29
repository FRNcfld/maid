package com.frnc.misc.mechanics.apotheotic_creation;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 神化 (Apotheosis) 词缀物品的"身份"匹配工具。
 *
 * Create 的仓库管理员 (Stock Keeper) / 物流系统用 ItemHandlerHelper.canItemStacksStack
 * 与 BigItemStack.equals 进行严格 NBT 匹配, 神化词缀物品 NBT 含随机词缀数值/耐久等,
 * 导致无法按词缀取出。本工具按"物品 + 稀有度 + 词缀 ID 集合"判定身份, 忽略 NBT 数值差异。
 */
public class ApotheosisItemMatcher
{
    /** Apotheosis 物品词缀数据的 NBT 键 (AffixHelper.AFFIX_DATA) */
    private static final String AFFIX_DATA_TAG = "affix_data";

    /** 该物品是否为神化词缀装备 */
    public static boolean isAffixItem(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(AFFIX_DATA_TAG);
    }

    /** 两个物品是否具有相同的词缀身份 (同物品 + 同稀有度 + 同词缀 ID 集合, 忽略数值/耐久等 NBT) */
    public static boolean sameAffixIdentity(ItemStack a, ItemStack b)
    {
        if (!a.is(b.getItem())) return false;

        DynamicHolder<LootRarity> rarityA = AffixHelper.getRarity(a);
        DynamicHolder<LootRarity> rarityB = AffixHelper.getRarity(b);
        if (!rarityA.isBound() || !rarityB.isBound()) return false;
        if (rarityA.get() != rarityB.get()) return false;

        Set<String> idsA = affixIds(a);
        Set<String> idsB = affixIds(b);
        return idsA.equals(idsB);
    }

    /** 词缀 ID 集合 (已排序, 保证顺序无关) */
    private static Set<String> affixIds(ItemStack stack)
    {
        Map<DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        return affixes.keySet().stream()
                .filter(DynamicHolder::isBound)
                .map(holder -> holder.getId().toString())
                .collect(Collectors.toSet());
    }
}
