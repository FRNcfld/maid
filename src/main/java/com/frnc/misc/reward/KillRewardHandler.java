package com.frnc.misc.reward;

import com.frnc.misc.Misc;
import com.mojang.logging.LogUtils;
import dev.xkmc.l2hostility.content.logic.DifficultyLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * 击杀掉落机制: 玩家击杀 L2Hostility 恶意怪物时, 按概率掉落数据包 data/misc/kill_reward/rewards.json 中配置的物品。
 *
 * 掉落概率 = 怪物恶意等级 X * 配置项 chance Y。
 * X 为 0 (未获得恶意等级的怪物) 时概率为 0, 不掉落; 概率 >= 1 时必然掉落。
 * 每条配置独立掷骰, 均可触发。
 */
@Mod.EventBusSubscriber(modid = Misc.MOD_ID)
public class KillRewardHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onLivingDeath(final LivingDeathEvent event)
    {
        // 仅服务器端处理
        if (event.getEntity().level().isClientSide) return;
        // 击杀者必须是玩家 (弓箭等投射物伤害 getEntity() 也会返回其主人玩家)
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        // 被击杀的是怪物 (敌对生物), 而非玩家
        if (!(event.getEntity() instanceof Monster monster)) return;

        // 怪物恶意等级 X (L2Hostility)
        int level = DifficultyLevel.ofAny(monster);
        if (level <= 0) return;

        // 逐条按概率 X*Y 掷骰, 命中则掉落对应物品
        for (KillRewardEntry entry : KillRewardConfig.getRewards())
        {
            double probability = level * entry.chance();
            if (probability <= 0) continue;
            if (monster.getRandom().nextDouble() >= probability) continue;

            ItemStack stack = resolveItem(entry);
            if (!stack.isEmpty())
            {
                spawnDrop(monster, stack);
                LOGGER.info("[misc] {} 击杀恶意等级 {} 怪物, 掉落 {}", player.getName().getString(), level, stack);
            }
        }
    }

    /** 在怪物死亡位置生成掉落物 */
    private static void spawnDrop(Monster monster, ItemStack stack)
    {
        ItemEntity drop = new ItemEntity(monster.level(), monster.getX(), monster.getY(), monster.getZ(), stack);
        drop.setPickUpDelay(10);
        monster.level().addFreshEntity(drop);
    }

    /** 解析物品; 未注册返回空栈 (配置写错不崩服, 仅跳过); 附加配置的 NBT (如附魔书) */
    private static ItemStack resolveItem(KillRewardEntry entry)
    {
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(entry.itemId());
        if (item.isEmpty())
        {
            LOGGER.warn("[misc] 击杀掉落物品 {} 未注册, 跳过", entry.itemId());
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item.get(), Math.max(1, entry.amount()));
        if (entry.nbt() != null)
        {
            stack.setTag(entry.nbt().copy());
        }
        return stack;
    }
}
