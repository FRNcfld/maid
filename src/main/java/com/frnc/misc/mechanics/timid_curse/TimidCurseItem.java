package com.frnc.misc.mechanics.timid_curse;

import com.frnc.misc.Config;
import com.google.common.collect.Multimap;
import dev.xkmc.l2hostility.content.item.curio.core.CurseCurioItem;
import dev.xkmc.l2hostility.content.logic.DifficultyLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * 「怯懦诅咒」(Curse of Timid) — L2 Hostility 诅咒类 Curio 饰品。
 *
 * 效果(数值以工具提示文案为准,2026-08-25 决定修正代码使其与文案一致):
 * <ul>
 *   <li>所受伤害减免:基础 10%,难度等级每提升 10 级额外减少 1%,上限 80%(即 700 级封顶)。</li>
 *   <li>基础近战攻击惩罚:难度等级每 10 级 -0.1 点,最多 -10 点(加法修饰符)。</li>
 *   <li>hostility 难度等级修正:-50(降低佩戴者难度)。</li>
 *   <li>作为恶意诅咒装备时,额外提供 2 个 {@code hostility_curse} 诅咒槽容量。</li>
 *   <li>死亡时永不掉落(ALWAYS_KEEP)。</li>
 *   <li>获取:玩家首次击杀难度 ≥ 阈值(默认 1000,见配置)的怪物时掉落(见 {@link TimidCurseDrops})。</li>
 * </ul>
 */
public class TimidCurseItem extends CurseCurioItem {

	private static final String CURSE_SLOT = "hostility_curse";
	private static final String LV_TAG = "lv";

	public TimidCurseItem(Properties properties) {
		super(properties);
	}

	/**
	 * L2 Hostility 诅咒框架钩子:返回额外难度等级修正,固定 -50 级。
	 */
	@Override
	public int getExtraLevel() {
		return -50;
	}

	/**
	 * 所受伤害减免。被 {@code MobEvents} 在佩戴者受到伤害时调用(LivingDamageEvent)。
	 * 每 10 级 +1%,基础 10%,上限 80%。
	 */
	@Override
	public void onDamage(ItemStack stack, LivingEntity entity, LivingDamageEvent event) {
		int level = DifficultyLevel.ofAny(entity);
		float reduction = (float) Math.min(0.8, 0.1 + level * 0.001);
		event.setAmount(event.getAmount() * (1.0f - reduction));
	}

	/**
	 * 每 tick 将佩戴者当前难度等级写入物品 NBT 的 "lv" 键(供外部读取)。
	 */
	@Override
	public void curioTick(SlotContext slotContext, ItemStack stack) {
		super.curioTick(slotContext, stack);
		if (slotContext.entity() != null) {
			stack.getOrCreateTag().putLong(LV_TAG, DifficultyLevel.ofAny(slotContext.entity()));
		}
	}

	/**
	 * 槽位修正 + 攻击惩罚。
	 * 注意:本版 L2 Hostility 的 {@code getAttributeModifiers(SlotContext, ...)} 为 final,
	 * 实际覆写点是被其转调的 protected 重载。
	 */
	@Override
	protected Multimap<Attribute, AttributeModifier> getAttributeModifiers(LivingEntity entity, UUID uuid) {
		Multimap<Attribute, AttributeModifier> map = super.getAttributeModifiers(entity, uuid);
		CuriosApi.addSlotModifier(map, CURSE_SLOT, uuid, 2.0, AttributeModifier.Operation.ADDITION); // 作为恶意诅咒装备时 +2 诅咒栏位
		if (entity != null) {
			int level = DifficultyLevel.ofAny(entity);
			float penalty = -Math.min(level * 0.01f, 10.0f); // 每 10 级 -0.1,最多 -10 点基础近战攻击
			map.put(Attributes.ATTACK_DAMAGE,
					new AttributeModifier(uuid, "misc.curse_of_timid.attack_penalty", penalty,
							AttributeModifier.Operation.ADDITION));
		}
		return map;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("tooltip.misc.curse_of_timid").withStyle(ChatFormatting.GOLD));
		tooltip.add(Component.translatable("tooltip.misc.curse_of_timid.1").withStyle(ChatFormatting.GOLD));
		// 获取途径提示使用鲜红色(猩红/动脉血红),与其他金色效果提示区分
		tooltip.add(Component.translatable("tooltip.misc.curse_of_timid.3",
				Config.curseOfTimidDropLevel).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xC8102E))));
	}

	/**
	 * 死亡不掉落。
	 */
	@Override
	public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel,
			boolean recentlyHit, ItemStack stack) {
		return ICurio.DropRule.ALWAYS_KEEP;
	}
}
