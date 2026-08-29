package com.frnc.misc.mechanics.liquidburner;

import com.frnc.misc.Misc;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * {@code misc:liquidburning} 配方类型与序列化器注册。
 */
public class RecipeRegistry
{
	public static final DeferredRegister<RecipeType<?>> TYPES =
			DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Misc.MOD_ID);
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
			DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Misc.MOD_ID);

	public static final RegistryObject<RecipeType<LiquidBurning>> LIQUIDBURNING = TYPES.register(
			"liquidburning", () -> new RecipeType<LiquidBurning>()
			{
				@Override
				public String toString()
				{
					return "liquidburning";
				}
			});

	public static final RegistryObject<RecipeSerializer<LiquidBurning>> LIQUIDBURNING_SERIALIZER =
			SERIALIZERS.register("liquidburning", () -> LiquidBurning.Serializer.INSTANCE);

	public static void register(IEventBus bus)
	{
		SERIALIZERS.register(bus);
		TYPES.register(bus);
	}
}
