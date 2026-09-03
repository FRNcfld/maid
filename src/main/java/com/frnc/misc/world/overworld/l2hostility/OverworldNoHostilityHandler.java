package com.frnc.misc.world.overworld.l2hostility;

import com.frnc.misc.Misc;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 机制: 主世界自然生成 (MobSpawnType.NATURAL) 的敌对生物不会获得 L2Hostility 恶意等级,
 * 但 spore 模组的生物 (实体注册 id 命名空间为 {@code spore}) 除外, 仍会正常获得等级与词条。
 *
 * 原理: L2Hostility 在 MobSpawnEvent.FinalizeSpawn (默认优先级 NORMAL) 中为生物定级。
 * 本处理以更高优先级 (HIGHEST) 抢先执行: 对符合条件的目标, 通过 {@link NoHostilityCap}
 * 把其 MobTraitCap 直接置为"已初始化的 0 级空状态"。此后 L2Hostility 的 initMob 看到
 * isInitialized() 为 true 便跳过等级/词条初始化 (不会叠加属性、词条或敌对掉落概率)。
 *
 * NoHostilityCap 的实现织入位于 com.frnc.misc.mixin.MobTraitCapMixin (mixin 需留在
 * misc.mixins.json 声明的 com.frnc.misc.mixin 包内), 二者以强转接口的形式协作。
 *
 * 范围界定:
 *  - 仅当维度为主世界 (minecraft:overworld) 且生成来源为 MobSpawnType.NATURAL。
 *  - 下界/末地、刷怪笼 (含神化)、刷怪蛋、玩家召唤、繁殖、命令等来源不受影响,
 *    保持 L2Hostility 原行为 (与 apotheotic_l2hostility 的刷怪笼补等级不冲突)。
 */
@Mod.EventBusSubscriber(modid = Misc.MOD_ID)
public class OverworldNoHostilityHandler
{
    /** spore 模组 (真菌感染) 的生物命名空间, 这些生物仍然保留恶意等级 */
    private static final String SPORE_NAMESPACE = "spore";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFinalizeSpawn(final MobSpawnEvent.FinalizeSpawn event)
    {
        Mob mob = event.getEntity();
        if (mob.level().isClientSide) return;
        if (!isSubject(mob, event.getSpawnType())) return;
        // 非 L2Hostility 目标 (被动生物/配置排除类型) 不会定级, 无需处理
        if (!MobTraitCap.HOLDER.isProper(mob)) return;

        MobTraitCap cap = (MobTraitCap) MobTraitCap.HOLDER.get(mob);
        ((NoHostilityCap) (Object) cap).misc$suppressHostility();
    }

    /** 判断是否为"主世界自然生成且非 spore"的目标 */
    private static boolean isSubject(Mob mob, MobSpawnType spawnType)
    {
        if (spawnType != MobSpawnType.NATURAL) return false;        // 仅常规自然刷怪
        if (mob.level().dimension() != Level.OVERWORLD) return false; // 仅主世界
        return !isSpore(mob);
    }

    /** spore 例外: 实体注册 id 的命名空间为 "spore" */
    private static boolean isSpore(Mob mob)
    {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        return key != null && SPORE_NAMESPACE.equals(key.getNamespace());
    }
}
