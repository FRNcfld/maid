package com.frnc.misc.mechanics.recall_potion;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * 「回归药水」(Recall Potion) — 长按饮用后传送回个人复活点的消耗品。
 *
 * <p>移植自 NeoForge 1.21.1 模组 c6c 的同名物品(见 {@code src/main/recall_potion.md}),
 * 行为等价,但底层 API 换成了 1.20.1 的对应实现。
 *
 * <ul>
 *   <li>饮用耗时 32 tick(1.6 秒)与饮用音效均与原版药水一致,播放喝药水的手臂动画。</li>
 *   <li>传送目标 = 玩家个人的复活点(床 / 重生锚),跨维度有效。</li>
 *   <li>饮用后退还一个玻璃瓶,背包满则原地掉落;创造模式既不消耗也不返还。</li>
 *   <li>获取:玩家击杀带恶意等级的敌对生物时按概率掉落
 *       (配置在 {@code data/misc/kill_reward/rewards.json},概率 = 怪物恶意等级 × 0.001);
 *       也可从创造模式标签页(L2Hostility 页)或 {@code /give @p misc:recall_potion} 取得。</li>
 * </ul>
 *
 * <p><strong>与上游的刻意差异</strong>
 * <ol>
 *   <li>上游在末地维度禁用本物品,本模组按需求<strong>不限制任何维度</strong>。</li>
 *   <li>上游无 tooltip,本模组补了中英双语的 {@code appendHoverText}。</li>
 *   <li>英文名用 Title Case {@code "Recall Potion"};上游是全小写 {@code "recall potion"},
 *       文档 §7 指出那是文案疏忽(与同模组其他物品风格不一致)。</li>
 *   <li>重生锚充能的处理按<em>意图</em>而非字面量:见 {@link #teleportToRespawn} 内的说明。</li>
 *   <li>饮用时长(上游 100 tick)与饮用音效(上游 {@code HONEY_DRINK})均改为与原版药水一致,
 *       即 32 tick 与 {@code GENERIC_DRINK}。</li>
 *   <li>上游没有任何战利品表(文档 §6),本模组额外挂了本仓库既有的 kill_reward 掉落。</li>
 * </ol>
 *
 * <p><strong>移植用到的 1.20.1 等价 API</strong>(上游是 1.21.1,下列 API 在本版不存在):
 * {@code DimensionTransition} → 直接调 {@code ServerPlayer#teleportTo};
 * {@code ServerPlayer#findRespawnPositionAndUseSpawnBlock} → 静态的
 * {@code Player#findRespawnPositionAndUseSpawnBlock};
 * {@code Player#hasInfiniteMaterials} → {@code Player#isCreative};
 * {@code ItemStack#consume} → {@code ItemStack#shrink};
 * {@code MinecraftServer#isLevelEnabled} → {@code getLevel(key) != null};
 * {@code Entity#canChangeDimensions(level, target)} → 无参版。
 */
public class RecallPotionItem extends Item {

	/**
	 * 饮用耗时,单位 tick(20 tick = 1 秒)。
	 * 取 32 与原版药水({@code PotionItem})一致,而不是上游的 100(5 秒)。
	 */
	private static final int DRINK_DURATION = 32;

	public RecallPotionItem(Properties properties) {
		super(properties);
	}

	/**
	 * 右键立即进入使用状态(无需先按住),随后由 {@link #getUseDuration} 决定何时喝完。
	 */
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		return ItemUtils.startUsingInstantly(level, player, hand);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return DRINK_DURATION;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}

	// 音效刻意不覆写:继承 Item 默认的 SoundEvents.GENERIC_DRINK,
	// 与原版药水(PotionItem 同样不覆写)完全一致。上游用的是 HONEY_DRINK。

	/**
	 * 饮用完成:先传送(仅服务端),再处理物品消耗与玻璃瓶返还。
	 *
	 * <p>原版 {@code LivingEntity#completeUsingItem} 会把<strong>真实的手持堆叠</strong>传进来,
	 * 并在返回值与它不同时写回手上,因此这里"就地 shrink + 返回新玻璃瓶"的写法是安全的。
	 */
	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (entity instanceof ServerPlayer serverPlayer) {
			teleportToRespawn(serverPlayer);
		}

		// 非玩家实体:直接消耗(正常游戏流程不会走到这里)
		if (!(entity instanceof Player player)) {
			stack.shrink(1);
			return stack;
		}

		// 创造模式既不消耗也不返还玻璃瓶
		if (player.isCreative()) {
			return stack;
		}

		stack.shrink(1);
		if (stack.isEmpty()) {
			// 最后一个:把手上的空位换成玻璃瓶
			return new ItemStack(Items.GLASS_BOTTLE);
		}

		// 还有剩余:额外塞一个玻璃瓶进背包,背包满则原地掉落
		ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
		if (!player.getInventory().add(bottle)) {
			player.drop(bottle, false);
		}
		return stack;
	}

	/**
	 * 把玩家传送回个人复活点。
	 *
	 * <p>目标解析完全交给原版 {@code Player#findRespawnPositionAndUseSpawnBlock}:
	 * 它内部已按方块类型区分床 / 重生锚并计算站位,末位布尔为 {@code true} 表示
	 * <strong>不消耗重生锚充能</strong>(见方法内注释)。
	 *
	 * <p>退化路径(从未设过复活点、复活点所在维度未加载、或床 / 重生锚已失效)与
	 * 原版 {@code PlayerList#respawn} 一致:回主世界共享出生点。
	 */
	private static void teleportToRespawn(ServerPlayer player) {
		MinecraftServer server = player.getServer();
		ServerLevel respawnLevel = server.getLevel(player.getRespawnDimension());
		BlockPos respawnPos = player.getRespawnPosition();

		ServerLevel target;
		Vec3 dest;
		float yaw;
		if (respawnLevel != null && respawnPos != null) {
			// 末位布尔:1.20.1 里决定扣充能的条件是 (!isRespawnForced && !该布尔),
			// 所以传 true 才是"不消耗重生锚充能"。上游 1.21.1 传的是 false,
			// 但文档 §4.5 明确说明该布尔语义随版本有变、且声明意图是"不消耗",
			// 故此处按意图取 true。改传 false 会变得跟在该锚处死亡复活一样扣充能。
			Optional<Vec3> found = Player.findRespawnPositionAndUseSpawnBlock(
					respawnLevel, respawnPos, player.getRespawnAngle(), player.isRespawnForced(), true);
			if (found.isPresent()) {
				target = respawnLevel;
				dest = found.get();
				yaw = player.getRespawnAngle();
			} else {
				// 床 / 重生锚已失效
				target = server.overworld();
				dest = Vec3.atBottomCenterOf(target.getSharedSpawnPos());
				yaw = target.getSharedSpawnAngle();
			}
		} else {
			// 从未设过复活点,或复活点所在维度未加载
			target = server.overworld();
			dest = Vec3.atBottomCenterOf(target.getSharedSpawnPos());
			yaw = target.getSharedSpawnAngle();
		}

		// 同维度直接传送;跨维度需要额外校验(例如骑乘着无法换维度的载具)
		if (!player.level().dimension().equals(target.dimension()) && !player.canChangeDimensions()) {
			return;
		}
		player.teleportTo(target, dest.x, dest.y, dest.z, yaw, 0.0F);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("tooltip.misc.recall_potion").withStyle(ChatFormatting.GOLD));
		tooltip.add(Component.translatable("tooltip.misc.recall_potion.1").withStyle(ChatFormatting.GOLD));
	}
}
