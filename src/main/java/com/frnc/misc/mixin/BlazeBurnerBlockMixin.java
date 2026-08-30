package com.frnc.misc.mixin;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Create 6.0.8 中 {@link BlazeBurnerBlock#newBlockEntity} 在热度为 NONE 时返回 null(方块实体不存在),
 * 导致熄灭的燃烧器储罐数据随方块实体一起丢失。本 Mixin 使任何状态下都创建方块实体,
 * 保证液体燃料储罐始终持久化(与 Create 0.5.x 行为一致)。
 *
 * 注意: {@code newBlockEntity} 是原版 {@code Block} 的方法 (BlazeBurnerBlock 只是覆写), 生产环境
 * 该方法被映射为 SRG 名 {@code m_142194_}, 因此这里**不能加 remap=false** (否则生产环境按字面名
 * 找不到目标导致 Mixin 应用失败)。让 refmap 将其映射为 SRG 名即可, dev 环境由 mixin.env.remapRefMap
 * 再映射回 parchment 名 —— 与 NourishmentEffectMixin 对 {@code applyEffectTick} 的处理一致。
 * (BlazeBurnerBlockEntityMixin 目标为 Create 自身方法, 生产环境保留原名, 才用 remap=false。)
 */
@Mixin(BlazeBurnerBlock.class)
public abstract class BlazeBurnerBlockMixin
{
	@Inject(method = "newBlockEntity", at = @At("HEAD"), cancellable = true)
	private void misc$alwaysCreateBlockEntity(BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir)
	{
		cir.setReturnValue(AllBlockEntityTypes.HEATER.get().create(pos, state));
	}
}
