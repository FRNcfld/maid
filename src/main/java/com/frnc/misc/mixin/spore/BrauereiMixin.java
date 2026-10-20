package com.frnc.misc.mixin.spore;

import com.Harbinger.Spore.Sentities.Organoids.Brauerei;
import com.frnc.misc.mechanics.gas_mask_curio.GasMaskProtection;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * spore 防毒判定点: {@code Brauerei#spreadDeBuffs} 里的 lambda —— 扩散负面效果。
 * 把判定里的 {@code getItemBySlot(HEAD)} 换成"首饰栏面具优先", 其余装备查询不受影响。
 *
 * <p>注意目标方法是编译器生成的 lambda 名({@code lambda$spreadDeBuffs$1});
 * spore 换版本重编译后该名字可能变化, 届时此注入会启动失败而不是静默失效。
 */
@Mixin(Brauerei.class)
public abstract class BrauereiMixin
{
    @Redirect(method = "lambda$spreadDeBuffs$1", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack misc$gasMaskAsHead(LivingEntity entity, EquipmentSlot slot)
    {
        return GasMaskProtection.headItemForMaskChecks(entity, slot);
    }
}
