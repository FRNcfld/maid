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
 */
@Mixin(BlazeBurnerBlock.class)
public abstract class BlazeBurnerBlockMixin
{
	@Inject(method = "newBlockEntity", at = @At("HEAD"), cancellable = true, remap = false)
	private void misc$alwaysCreateBlockEntity(BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir)
	{
		cir.setReturnValue(AllBlockEntityTypes.HEATER.get().create(pos, state));
	}
}
