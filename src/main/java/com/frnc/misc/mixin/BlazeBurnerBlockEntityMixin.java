package com.frnc.misc.mixin;

import com.frnc.misc.mechanics.liquidburner.BlazeTank;
import com.frnc.misc.mechanics.liquidburner.FluidContainer;
import com.frnc.misc.mechanics.liquidburner.LiquidBurning;
import com.frnc.misc.mechanics.liquidburner.RecipeRegistry;
import com.frnc.misc.mechanics.liquidburner.Tags;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * 让 Create 烈焰人燃烧器({@link BlazeBurnerBlockEntity})支持液体燃料。
 * 参考 LiquidBurner 模组技术文档实现:
 * <ul>
 *   <li>注入 1000 mB 流体储罐并暴露 {@code FLUID_HANDLER} 能力,机械管道/机械手可直接灌入液体。</li>
 *   <li>满罐时消耗整罐作为一次燃料:优先匹配 {@code misc:liquidburning} 配方,否则按标签回退(特殊 1000 / 普通 1600 tick)。</li>
 *   <li>配方 {@code superheattime > 0} 使燃烧器进入喷火(SPECIAL)状态,持续加热时间由 {@code @ModifyConstant} 接管。</li>
 * </ul>
 *
 * 持久化注意(Create 6.0.8 特有):
 * <ul>
 *   <li>Create 6.0.8 中 {@code BlazeBurnerBlock.newBlockEntity} 在热度 NONE 时返回 null(方块实体不存在),
 *       熄灭的燃烧器储罐数据会随方块实体丢失 —— 由 {@link BlazeBurnerBlockMixin} 强制始终创建方块实体解决。</li>
 *   <li>恢复储罐 NBT 时 {@code SmartFluidTank} 会触发内容回调,若此时直接消耗会把刚恢复的燃料立刻排空,
 *       故读取期间用 {@code loading} 标志抑制消耗。</li>
 * </ul>
 */
@Mixin(BlazeBurnerBlockEntity.class)
public abstract class BlazeBurnerBlockEntityMixin extends SmartBlockEntity
{
	@Shadow(remap = false)
	protected BlazeBurnerBlockEntity.FuelType activeFuel;
	@Shadow(remap = false)
	protected int remainingBurnTime;
	@Shadow(remap = false)
	protected void playSound()
	{
	}
	@Shadow(remap = false)
	public void updateBlockState()
	{
	}

	@Unique
	private final BlazeTank tank = new BlazeTank(1000, fluidStack -> tryConsumeLiquid());
	@Unique
	private final LazyOptional<IFluidHandler> lazy = LazyOptional.of(() -> tank);
	/** superheat 配方的普通燃烧时间缓存,供 {@code misc$addBurntime} 消费 */
	@Unique
	private LiquidBurning lb;
	/** 正在从 NBT 读取储罐数据(防止读取时回调触发消耗/排空) */
	@Unique
	private boolean loading;

	protected BlazeBurnerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	/** 对外暴露流体处理能力,Create 机械手/流体管道可直接灌入 */
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side)
	{
		if (cap == ForgeCapabilities.FLUID_HANDLER)
		{
			return lazy.cast();
		}
		return super.getCapability(cap, side);
	}

	/** 储罐内容变化回调 + 燃料耗尽时调用;前提:储罐必须恰好满 */
	@Unique
	private void tryConsumeLiquid()
	{
		if (loading)
		{
			return; // NBT 加载期间不消耗燃料,防止刚恢复的储罐被立刻排空
		}
		if (tank.getFluidAmount() != tank.getCapacity())
		{
			return;
		}
		if (recipeFluids())
		{
			return; // 配方命中后已处理
		}
		BlazeBurnerBlockEntity.FuelType newFuel = BlazeBurnerBlockEntity.FuelType.NONE;
		int newBurnTime = 0;
		var fluid = tank.getFluid().getFluid();
		if (fluid.is(Tags.BLAZE_BURNER_FUEL_SPECIAL))
		{
			newFuel = BlazeBurnerBlockEntity.FuelType.SPECIAL;
			newBurnTime = 1000;
		}
		else if (fluid.is(Tags.BLAZE_BURNER_FUEL_REGULAR))
		{
			newFuel = BlazeBurnerBlockEntity.FuelType.NORMAL;
			newBurnTime = 1600;
		}
		consumeLiquid(newFuel, newBurnTime);
	}

	/** 配方优先匹配:命中 superheat 配方则保留 lb 供 @ModifyConstant 使用 */
	@Unique
	private boolean recipeFluids()
	{
		if (level == null)
		{
			return false;
		}
		FluidContainer container = new FluidContainer(tank.getFluid());
		Optional<LiquidBurning> recipe = level.getRecipeManager()
				.getRecipeFor(RecipeRegistry.LIQUIDBURNING.get(), container, level);
		if (recipe.isEmpty())
		{
			lb = null;
			return false;
		}
		LiquidBurning found = recipe.get();
		BlazeBurnerBlockEntity.FuelType newFuel;
		int newBurnTime;
		if (found.getSuperheattime() > 0)
		{
			newFuel = BlazeBurnerBlockEntity.FuelType.SPECIAL;
			newBurnTime = found.getSuperheattime();
			lb = found;
		}
		else
		{
			newFuel = BlazeBurnerBlockEntity.FuelType.NORMAL;
			newBurnTime = found.getBurntime();
			lb = null;
		}
		consumeLiquid(newFuel, newBurnTime);
		return true;
	}

	/** 门控 + 状态写入 + 排空整罐 */
	@Unique
	private void consumeLiquid(BlazeBurnerBlockEntity.FuelType newFuel, int newBurnTime)
	{
		if (newFuel == BlazeBurnerBlockEntity.FuelType.NONE)
		{
			return;
		}
		// 禁止降级:NONE(0) < NORMAL(1) < SPECIAL(2)
		if (newFuel.ordinal() < activeFuel.ordinal())
		{
			return;
		}
		// 喷火期间不换燃料,保证喷火动画稳定
		if (activeFuel == BlazeBurnerBlockEntity.FuelType.SPECIAL && remainingBurnTime > 20)
		{
			return;
		}
		if (newFuel == activeFuel)
		{
			// 同种燃料叠加,上限 10000 tick
			if (remainingBurnTime + newBurnTime > 10000)
			{
				return;
			}
			remainingBurnTime = Mth.clamp(remainingBurnTime + newBurnTime, 0, 10000);
		}
		else
		{
			remainingBurnTime = newBurnTime;
		}
		activeFuel = newFuel;
		if (!level.isClientSide)
		{
			playSound();
			updateBlockState();
		}
		tank.drain(tank.getCapacity(), IFluidHandler.FluidAction.EXECUTE);
	}

	/** 把 Create 维持"烈焰(kindled)"状态的常量 5000 替换为 superheat 配方的普通燃烧时间 */
	@ModifyConstant(method = "tick", constant = @Constant(intValue = 5000), remap = false)
	public int misc$addBurntime(int constant)
	{
		if (lb != null)
		{
			int temp = lb.getBurntime();
			lb = null;
			return temp;
		}
		return constant;
	}

	/** 燃料耗尽时用满罐液体续料(在 tick 中第二次 updateBlockState 之前) */
	@Inject(method = "tick", at = @At(value = "INVOKE",
			target = "Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlockEntity;updateBlockState()V",
			shift = At.Shift.BEFORE, ordinal = 1), remap = false)
	public void misc$appendTick(CallbackInfo ci)
	{
		if (remainingBurnTime <= 0)
		{
			tryConsumeLiquid();
		}
	}

	@Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Z)V", at = @At("HEAD"), remap = false)
	public void misc$appendRead(CompoundTag compound, boolean clientPacket, CallbackInfo ci)
	{
		// 读取储罐期间置 loading,避免 SmartFluidTank 恢复内容时触发回调把刚恢复的燃料立刻消耗/排空;
		// 仅在存在 "tank" 键时恢复,防止缺失该键的部分同步包把储罐清空
		if (compound.contains("tank"))
		{
			loading = true;
			try
			{
				tank.readFromNBT(compound.getCompound("tank"));
			}
			finally
			{
				loading = false;
			}
		}
	}

	@Inject(method = "write(Lnet/minecraft/nbt/CompoundTag;Z)V", at = @At("HEAD"), remap = false)
	public void misc$appendWrite(CompoundTag compound, boolean clientPacket, CallbackInfo ci)
	{
		CompoundTag tag = new CompoundTag();
		tank.writeToNBT(tag);
		compound.put("tank", tag);
	}
}
