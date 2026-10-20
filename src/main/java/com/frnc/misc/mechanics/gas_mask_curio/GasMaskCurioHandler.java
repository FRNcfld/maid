package com.frnc.misc.mechanics.gas_mask_curio;

import com.Harbinger.Spore.Core.Sitems;
import com.frnc.misc.Misc;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 机制: 让 spore (真菌感染) 的「防毒面具」({@code spore:gas_mask}) 可以装备在 Curios 的
 * 「首饰」({@code head}) 栏位。
 *
 * <p>光加标签不够: {@code GasMaskItem} 只是普通的 {@code ArmorItem}, 没有实现 Curios 的
 * {@code ICurioItem}。Curios 只给"实现了 ICurioItem (或在它的注册表里登记过) 的物品"挂上
 * {@code ICurio} 能力, 没有能力的物品放进饰品栏会被立即弹回。这里用 Curios 官方提供的
 * {@link CuriosApi#registerCurio} 把该物品登记为一个"全是默认行为"的 curio。
 *
 * <p>另一半配置是数据包标签 {@code data/curios/tags/items/head.json}: 槽位的校验器是
 * {@code curios:tag}, 不在标签里的物品即使实现了 ICurioItem 也进不了该槽位。
 *
 * <p>装备后"防毒效果生效"由 {@link GasMaskProtection} 与 {@code com.frnc.misc.mixin.spore} 包下的
 * 各 Mixin 负责: 只在 spore 逐处 {@code getItemBySlot(HEAD)} 的调用点替换成"首饰栏面具优先",
 * 不覆写原版装备查询。因此饰品栏里的面具<strong>只提供防毒效果, 不提供护甲</strong>,
 * 护甲仍以头盔栏为准; 同时戴着普通头盔也不影响防毒判定。
 *
 * <p>说明文字由下面的 {@link ICurioItem#getAttributesTooltip} 追加: Curios 把该方法的返回值
 * 追加到工具提示末尾, 所以在 "戴在头上时：" 护甲段之后会多出
 * "佩戴首饰时：免疫真菌感染与孢子毒气（不提供护甲）" 这一段, 与异色眼镜等物品的效果区排版一致。
 */
public class GasMaskCurioHandler
{
    @Mod.EventBusSubscriber(modid = Misc.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class EventHandler
    {
        /** 所有模组注册项均已就绪, 且尚未创建世界/玩家 —— 注册 curio 的最佳时机 */
        @SubscribeEvent
        public static void onCommonSetup(final FMLCommonSetupEvent event)
        {
            CuriosApi.registerCurio(Sitems.GAS_MASK.get(), new ICurioItem()
            {
                /**
                 * 在工具提示末尾追加效果区: 空行 + 金色 "佩戴首饰时：" + 效果说明。
                 *
                 * <p>用 getAttributesTooltip 而不是 getSlotsTooltip: 后者返回的列表会被插到
                 * 物品名之后(排在"戴在头上时："护甲段前面), 而本方法返回的内容由 Curios 追加到末尾,
                 * 与异色眼镜等物品"栏位 → 属性/效果区"的排版一致。
                 */
                @Override
                public List<Component> getAttributesTooltip(List<Component> tooltip, ItemStack stack)
                {
                    tooltip.add(Component.empty());
                    tooltip.add(Component.translatable("curios.modifiers.head").withStyle(ChatFormatting.GOLD));
                    tooltip.add(Component.translatable("tooltip.misc.gas_mask_curio")
                            .withStyle(ChatFormatting.GRAY));
                    return tooltip;
                }
            });
        }
    }
}
