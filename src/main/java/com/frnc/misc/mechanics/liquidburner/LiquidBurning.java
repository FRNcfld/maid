package com.frnc.misc.mechanics.liquidburner;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 液体燃烧配方:液体 → (普通燃烧时间, 喷火燃烧时间)。
 * 每次燃烧消耗一整罐(1000 mB),故 {@code burntime}/{@code superheattime} 即"每 1000 mB 液体的燃烧 tick 数"。
 */
public class LiquidBurning implements Recipe<FluidContainer>
{
	private final ResourceLocation id;
	private final FluidStack fluid;
	private final int burntime;
	private final int superheattime;

	public LiquidBurning(ResourceLocation id, FluidStack fluid, int burntime, int superheattime)
	{
		this.id = id;
		this.fluid = fluid;
		this.burntime = burntime;
		this.superheattime = superheattime;
	}

	public FluidStack getFluid()
	{
		return fluid;
	}

	public int getBurntime()
	{
		return burntime;
	}

	public int getSuperheattime()
	{
		return superheattime;
	}

	@Override
	public boolean matches(FluidContainer container, Level level)
	{
		return fluid.isFluidEqual(container.getFluid());
	}

	@Override
	public ItemStack assemble(FluidContainer container, RegistryAccess access)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return true;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess access)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public ResourceLocation getId()
	{
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return Serializer.INSTANCE;
	}

	@Override
	public RecipeType<?> getType()
	{
		return RecipeRegistry.LIQUIDBURNING.get();
	}

	public static class Serializer implements RecipeSerializer<LiquidBurning>
	{
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public LiquidBurning fromJson(ResourceLocation id, JsonObject json)
		{
			String fluidName = GsonHelper.getAsString(json, "fluid");
			ResourceLocation fluidId = ResourceLocation.tryParse(fluidName);
			Fluid fluid = fluidId == null ? null : ForgeRegistries.FLUIDS.getValue(fluidId);
			if (fluid == null)
			{
				throw new IllegalArgumentException("Unknown fluid: " + fluidName);
			}
			int burntime = GsonHelper.getAsInt(json, "burntime");
			int superheattime = GsonHelper.getAsInt(json, "superheattime", 0);
			return new LiquidBurning(id, new FluidStack(fluid, 1000), burntime, superheattime);
		}

		@Override
		public LiquidBurning fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
		{
			Fluid fluid = buf.readRegistryIdSafe(Fluid.class);
			int burntime = buf.readVarInt();
			int superheattime = buf.readVarInt();
			return new LiquidBurning(id, new FluidStack(fluid, 1000), burntime, superheattime);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, LiquidBurning recipe)
		{
			buf.writeRegistryId(ForgeRegistries.FLUIDS, recipe.fluid.getFluid());
			buf.writeVarInt(recipe.burntime);
			buf.writeVarInt(recipe.superheattime);
		}
	}
}
