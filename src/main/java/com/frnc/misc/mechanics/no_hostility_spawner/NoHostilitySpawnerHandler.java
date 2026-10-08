package com.frnc.misc.mechanics.no_hostility_spawner;

import com.frnc.misc.Misc;
import com.frnc.misc.world.overworld.l2hostility.NoHostilityCap;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.init.registrate.LHItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 机制: 让玩家按<strong>单个刷怪笼</strong>决定它刷出的怪是否带 L2Hostility 恶意等级。
 *
 * <ul>
 *   <li>手持「恶意吸收宝珠」({@code l2hostility:hostility_orb}) 右键刷怪笼 → 该刷怪笼「已净化」,
 *       之后刷出的怪不再获得恶意等级与词条;</li>
 *   <li>手持「恶意精华」({@code l2hostility:hostility_essence}) 右键同一个刷怪笼 → 恢复,
 *       之后刷出的怪重新获得恶意等级与词条。</li>
 * </ul>
 * 两种物品都在状态<strong>实际发生改变</strong>时消耗 1 个 (创造模式不消耗)。
 *
 * <p>原理: 与 {@link com.frnc.misc.world.overworld.l2hostility.OverworldNoHostilityHandler} 同一套手法 ——
 * 在 L2Hostility 定级之前 (FinalizeSpawn 的 HIGHEST 优先级; L2Hostility 自身的 CapabilityEvents 是默认
 * NORMAL 优先级), 通过 {@link NoHostilityCap#misc$suppressHostility()} 把 MobTraitCap 直接置为
 * "已初始化的 0 级空状态"。此后 L2Hostility 的 initMob 看到 isInitialized() 为 true 便跳过等级/词条初始化。
 *
 * <p>刷怪笼的身份由 Forge 的 {@link MobSpawnEvent.FinalizeSpawn#getSpawner()} 给出 —— 原版
 * BaseSpawner 与神化的 SpawnerLogicExt 都以 (this) 触发 ForgeEventFactory.onFinalizeSpawnSpawner,
 * 其内部 BaseSpawner 覆写了 getSpawnerBlockEntity() 返回该方块实体, 故无需 mixin, 也无需
 * "当前正在刷怪的刷怪笼" 这类静态上下文。
 *
 * <p>状态存在刷怪笼方块实体的 ForgeData ({@link BlockEntity#getPersistentData()}) 里, 键为
 * {@value #NBT_NO_HOSTILITY}。Forge 随 load/saveAdditional 自动存读该 compound, 因此标记跟着方块 NBT 走
 * (丝触 / NBT 搬运后保留), 而新放置的刷怪笼天然是"未净化"。不需要自定义 capability, 也不需要 SavedData。
 *
 * <p>范围界定:
 *  <ul>
 *    <li>仅处理 {@link SpawnerBlockEntity} (即 {@code minecraft:spawner})。神化 (Apotheosis) 刷怪笼
 *        ApothSpawnerTile 继承 SpawnerBlockEntity 且同样走 onFinalizeSpawnSpawner, 因此一并覆盖。</li>
 *    <li>不处理 L2Hostility 自带的「恶意刷怪笼」(TraitSpawnerBlockEntity): 它是另一种方块, 有自己
 *        "击杀全部怪物即净化子区块" 的机制。</li>
 *    <li>刻意<strong>不限制维度</strong> (与只管主世界的 OverworldNoHostilityHandler 不同):
 *        本机制的语义是"这个刷怪笼", 与维度无关。</li>
 *  </ul>
 *
 * <p>与既有机制的协作:
 *  <ul>
 *    <li>{@link com.frnc.misc.mechanics.apotheotic_l2hostility.ApotheoticL2HostilityHandler} 只在 cap
 *        未初始化时补初始化, 被本机制抑制过的生物不会被它"救回来"。</li>
 *    <li>OverworldNoHostilityHandler 同为 HIGHEST 优先级但只认 MobSpawnType.NATURAL, 对刷怪笼直接返回,
 *        二者互不干扰; 且抑制是幂等的。</li>
 *    <li>不取消 FinalizeSpawn: 取消只会跳过 Mob#finalizeSpawn 而生物仍会生成, 还会抹掉原版刷怪加成。</li>
 *  </ul>
 */
@Mod.EventBusSubscriber(modid = Misc.MOD_ID)
public class NoHostilitySpawnerHandler
{
    /** 刷怪笼方块实体 ForgeData 里的标记键: 为 true 表示"不再刷出带恶意等级的怪物" */
    private static final String NBT_NO_HOSTILITY = Misc.MOD_ID + ":no_hostility";

    /**
     * 右键刷怪笼切换状态。
     *
     * <p>注意本事件在<strong>客户端与服务端各触发一次</strong>, 且两侧都会在 isCanceled() 时直接返回
     * {@code getCancellationResult()}。因此:
     * <ol>
     *   <li>两端都必须取消 —— 只取消服务端时, 客户端会继续走 useItem 并发出 ServerboundUseItemPacket,
     *      服务端照样执行宝珠 use() 里的子区块净化;</li>
     *   <li>必须显式设成 {@link InteractionResult#SUCCESS} —— cancellationResult 默认是 PASS,
     *      而 PASS 不会让客户端止步 (SUCCESS.consumesAction() 才会)。</li>
     * </ol>
     */
    @SubscribeEvent
    public static void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event)
    {
        if (event.isCanceled()) return; // 别的模组已处理过这次右键: 不插手, 也不覆盖它设置的返回值

        ItemStack stack = event.getItemStack();
        boolean purify = stack.is(LHItems.HOSTILITY_ORB.get());
        boolean restore = stack.is(LHItems.HOSTILITY_ESSENCE.get());
        if (!purify && !restore) return;

        Level level = event.getLevel();
        // 写状态前必须确认是刷怪笼方块实体; 其它方块 (含刷怪蛋等原版交互) 一概不碰
        if (!(level.getBlockEntity(event.getPos()) instanceof SpawnerBlockEntity spawner)) return;

        // 两端都取消, 且显式返回 SUCCESS (理由见方法注释)
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        // 以下全部是服务端逻辑 (写状态 / 扣物品 / 反馈)
        if (level.isClientSide) return;

        CompoundTag data = spawner.getPersistentData();
        if (data.getBoolean(NBT_NO_HOSTILITY) == purify) return; // 状态未变: 不消耗也不提示

        if (purify)
        {
            data.putBoolean(NBT_NO_HOSTILITY, true);
        }
        else
        {
            data.remove(NBT_NO_HOSTILITY);
        }
        spawner.setChanged(); // BaseSpawner 自己不标脏, 不调则刚点完就关服可能丢标记

        Player player = event.getEntity();
        if (!player.isCreative()) stack.shrink(1);

        level.playSound(null, event.getPos(),
                purify ? SoundEvents.CONDUIT_ACTIVATE : SoundEvents.CONDUIT_DEACTIVATE,
                SoundSource.BLOCKS, 0.8F, purify ? 1.2F : 0.8F);
        player.displayClientMessage(Component.translatable(purify
                ? "message.misc.no_hostility_spawner.on"
                : "message.misc.no_hostility_spawner.off"), true);
    }

    /**
     * 抑制被净化刷怪笼产出的生物的恶意等级。
     *
     * <p>HIGHEST 优先级保证在 L2Hostility (NORMAL) 定级之前完成抑制。
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFinalizeSpawn(final MobSpawnEvent.FinalizeSpawn event)
    {
        // 只有刷怪笼产出的生物才带 BaseSpawner (自然生成 / 刷怪蛋 / 召唤等一律为 null)
        BaseSpawner spawner = event.getSpawner();
        if (spawner == null) return;

        Mob mob = event.getEntity();
        if (mob.level().isClientSide) return;

        // 这里只读标记, 故不限定方块实体的具体类型: 任何模组的刷怪笼都能兼容,
        // 我们没写过标记的自然读到 false
        BlockEntity spawnerEntity = spawner.getSpawnerBlockEntity();
        if (spawnerEntity == null || !spawnerEntity.getPersistentData().getBoolean(NBT_NO_HOSTILITY)) return;

        // 非 L2Hostility 目标 (被动生物/配置排除类型) 不会定级, 无需处理
        if (!MobTraitCap.HOLDER.isProper(mob)) return;

        MobTraitCap cap = (MobTraitCap) MobTraitCap.HOLDER.get(mob);
        ((NoHostilityCap) (Object) cap).misc$suppressHostility();
    }
}
