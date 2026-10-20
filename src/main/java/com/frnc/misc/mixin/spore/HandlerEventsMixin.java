package com.frnc.misc.mixin.spore;

import com.Harbinger.Spore.sEvents.HandlerEvents;
import com.frnc.misc.mechanics.gas_mask_curio.GasMaskProtection;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * spore 防毒判定点: {@code ProtectFromEffect} —— 免疫真菌感染(MYCELIUM)效果。
 * 把判定里的 {@code getItemBySlot(HEAD)} 换成"首饰栏面具优先", 其余装备查询不受影响。
 */
@Mixin(HandlerEvents.class)
public abstract class HandlerEventsMixin
{
    @Redirect(method = "ProtectFromEffect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack misc$gasMaskAsHead(LivingEntity entity, EquipmentSlot slot)
    {
        return GasMaskProtection.headItemForMaskChecks(entity, slot);
    }
}
