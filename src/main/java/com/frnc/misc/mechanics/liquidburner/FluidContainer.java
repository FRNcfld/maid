package com.frnc.misc.mechanics.liquidburner;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * 将 {@link FluidStack} 包装为最小 {@link Container},使液体能作为标准配方输入参与
 * {@code RecipeManager.getRecipeFor} 的匹配。
 */
public class FluidContainer implements Container
{
	private final FluidStack fluid;

	public FluidContainer(FluidStack fluid)
	{
		this.fluid = fluid;
	}

	public FluidStack getFluid()
	{
		return fluid;
	}

	@Override
	public int getContainerSize()
	{
		return 0;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public ItemStack getItem(int index)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int index, int count)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int index, ItemStack stack)
	{
	}

	@Override
	public void setChanged()
	{
	}

	@Override
	public boolean stillValid(Player player)
	{
		return true;
	}

	@Override
	public void clearContent()
	{
	}
}
