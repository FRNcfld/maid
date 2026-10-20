package com.frnc.misc.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.xkmc.l2hostility.content.item.curio.misc.TripleStripCape;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.UUID;

/**
 * 改写 L2Hostility「三色披风」提供的 Curios 栏位加成。
 *
 * <p>原版 {@code TripleStripCape#getAttributeModifiers} 自己 new 一个空 multimap, 只写入
 * {@code back +3.0}。这里在 HEAD 直接取消并返回全新的 multimap, 因此<strong>替换</strong>掉原来的 +3,
 * 改为: 护符(charm) +4、戒指(ring) +2、手饰(hands) +2、背饰(back) +2、首饰(head) +1。
 *
 * <p>只覆写这一个方法, 披风的其他行为 (包括 {@code SingletonItem#canEquip} 的"每人限一件") 原样保留。
 *
 * <p>工具提示不需要额外写: Curios 会把这里的每个修饰符渲染成
 * "栏位: 背饰 / 佩戴背饰时: +4 护符 栏位 ...",与异色眼镜、宝石手套等物品一致 —— 前提是玩家确实拥有
 * {@code back} 栏位。该栏位由本模组的 {@code data/misc/curios/entities/player_back.json} 授予
 * (L2Hostility 自带的玩家栏位表里只有 head/charm/ring/hands/hostility_curse)。
 *
 * <p>{@code TripleStripCape} 是第三方模组类, 运行时不做混淆重映射, 故 {@code remap = false}。
 */
@Mixin(value = TripleStripCape.class, remap = false)
public abstract class TripleStripCapeMixin
{
    private static final int CHARM_SLOTS = 4;
    private static final int RING_SLOTS = 2;
    private static final int HANDS_SLOTS = 2;
    private static final int BACK_SLOTS = 2;
    private static final int HEAD_SLOTS = 1;

    @Inject(method = "getAttributeModifiers", at = @At("HEAD"), cancellable = true, remap = false)
    private void misc$slotModifiers(SlotContext slotContext, UUID uuid, ItemStack stack,
            CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir)
    {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        // 槽位标识符来自 Curios 内置槽位: charm 护符 / ring 戒指 / hands 手饰 / back 背饰 / head 首饰
        CuriosApi.addSlotModifier(map, "charm", uuid, CHARM_SLOTS, AttributeModifier.Operation.ADDITION);
        CuriosApi.addSlotModifier(map, "ring", uuid, RING_SLOTS, AttributeModifier.Operation.ADDITION);
        CuriosApi.addSlotModifier(map, "hands", uuid, HANDS_SLOTS, AttributeModifier.Operation.ADDITION);
        CuriosApi.addSlotModifier(map, "back", uuid, BACK_SLOTS, AttributeModifier.Operation.ADDITION);
        CuriosApi.addSlotModifier(map, "head", uuid, HEAD_SLOTS, AttributeModifier.Operation.ADDITION);
        cir.setReturnValue(map);
    }
}
