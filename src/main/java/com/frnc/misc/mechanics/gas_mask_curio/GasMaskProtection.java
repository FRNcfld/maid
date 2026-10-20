package com.frnc.misc.mechanics.gas_mask_curio;

import com.Harbinger.Spore.Core.Sitems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

/**
 * 供 spore 各处"是否戴着防毒面具"的判定重定向调用。
 *
 * <p>spore 的防毒判定写法都是:
 * <pre>{@code Utilities.helmetList().contains(entity.getItemBySlot(EquipmentSlot.HEAD).getItem())}</pre>
 * 只认头盔栏。本类提供 {@link #headItemForMaskChecks} 顶替其中的 {@code getItemBySlot(HEAD)} 调用:
 * 玩家在 Curios 的「首饰」({@code head})栏戴着防毒面具时返回面具, 否则原样返回头盔栏物品。
 *
 * <p><strong>只在 spore 的调用点替换</strong>(见 {@code com.frnc.misc.mixin.spore} 包下各 Mixin),
 * 不覆写 {@code Player#getItemBySlot}。因此:
 * <ul>
 *   <li>原版装备变更检测看到的仍是真实头盔, <strong>护甲值只由头盔栏决定</strong>,
 *       饰品栏里的面具不提供任何护甲;</li>
 *   <li>其他模组查询头盔栏时也不受影响;</li>
 *   <li>防毒效果无论头盔栏是否为空都会生效 —— 可以同时戴头盔和面具。</li>
 * </ul>
 */
public final class GasMaskProtection
{
    private GasMaskProtection() {}

    /**
     * 替代 spore 判定里的 {@code entity.getItemBySlot(slot)}。
     *
     * @param entity 被判定是否受防毒面具保护的目标
     * @param slot   原调用传入的装备槽(只有 {@link EquipmentSlot#HEAD} 会被替换)
     * @return 玩家首饰栏里的防毒面具;否则返回真实的装备槽物品
     */
    public static ItemStack headItemForMaskChecks(LivingEntity entity, EquipmentSlot slot)
    {
        ItemStack real = entity.getItemBySlot(slot);
        if (slot != EquipmentSlot.HEAD) return real;
        if (!(entity instanceof Player player) || player.level().isClientSide) return real;

        Optional<ICuriosItemHandler> handler = CuriosApi.getCuriosInventory(player).resolve();
        if (handler.isEmpty()) return real;
        Optional<SlotResult> curio = handler.get().findFirstCurio(Sitems.GAS_MASK.get());
        return curio.isPresent() ? curio.get().stack() : real;
    }
}
