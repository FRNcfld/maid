package com.frnc.misc.mechanics.timid_curse;

import com.frnc.misc.Config;
import com.frnc.misc.Misc;
import dev.xkmc.l2hostility.content.logic.DifficultyLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 「怯懦诅咒」获取机制:玩家第一次击杀 L2 Hostility 难度 ≥ {@link Config#curseOfTimidDropLevel}(默认 1000)
 * 的怪物时,在该怪物死亡位置掉落一枚饰品。每位玩家仅触发一次,进度以玩家持久数据记录(跨死亡/重登保留)。
 */
@Mod.EventBusSubscriber(modid = Misc.MOD_ID)
public class TimidCurseDrops
{
	/** 玩家持久数据中记录「已获得怯懦诅咒」的键 */
	private static final String PLAYER_TAG = "misc_curse_of_timid_got";

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event)
	{
		// 仅服务器端处理
		if (event.getEntity().level().isClientSide) return;
		// 击杀者必须是玩家(弓箭等投射物伤害 getEntity() 也会返回其主人玩家)
		if (!(event.getSource().getEntity() instanceof Player player)) return;
		// 被击杀的是怪物,而非玩家
		if (event.getEntity() instanceof Player) return;

		// 怪物难度达到阈值才触发
		int level = DifficultyLevel.ofAny(event.getEntity());
		if (level < Config.curseOfTimidDropLevel) return;

		// 每位玩家只掉落一次
		CompoundTag data = player.getPersistentData();
		if (data.getBoolean(PLAYER_TAG)) return;
		data.putBoolean(PLAYER_TAG, true);

		// 在怪物死亡位置生成掉落物
		ItemStack stack = new ItemStack(TimidCurseItems.CURSE_OF_TIMID.get());
		ItemEntity drop = new ItemEntity(event.getEntity().level(), event.getEntity().getX(),
				event.getEntity().getY(), event.getEntity().getZ(), stack);
		drop.setPickUpDelay(10);
		event.getEntity().level().addFreshEntity(drop);
	}
}
