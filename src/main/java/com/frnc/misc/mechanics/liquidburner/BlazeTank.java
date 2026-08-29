package com.frnc.misc.mechanics.liquidburner;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

/**
 * 烈焰人燃烧器流体储罐:验证器只允许 {@code blaze_burner_fuel_all} 标签内的液体进入。
 * 标签之外的液体(即使写了配方)也无法泵入。
 */
public class BlazeTank extends SmartFluidTank
{
	public BlazeTank(int capacity, Consumer<FluidStack> updateCallback)
	{
		super(capacity, updateCallback);
	}

	@Override
	public boolean isFluidValid(FluidStack stack)
	{
		return stack.getFluid().is(Tags.BLAZE_BURNER_FUEL_ALL);
	}
}
