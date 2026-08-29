package com.frnc.misc.mechanics.liquidburner;

import com.frnc.misc.Misc;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/**
 * 液体燃料标签定义(数据包驱动):
 * <ul>
 *   <li>{@code blaze_burner_fuel_regular} — 普通燃料(默认 1600 tick)</li>
 *   <li>{@code blaze_burner_fuel_special} — 喷火/超热燃料(默认 1000 tick)</li>
 *   <li>{@code blaze_burner_fuel_all} — 储罐验证器门槛:允许进入储罐的全部液体</li>
 * </ul>
 */
public class Tags
{
	public static final TagKey<Fluid> BLAZE_BURNER_FUEL_SPECIAL = tag("blaze_burner_fuel_special");
	public static final TagKey<Fluid> BLAZE_BURNER_FUEL_REGULAR = tag("blaze_burner_fuel_regular");
	public static final TagKey<Fluid> BLAZE_BURNER_FUEL_ALL = tag("blaze_burner_fuel_all");

	private static TagKey<Fluid> tag(String name)
	{
		return FluidTags.create(ResourceLocation.fromNamespaceAndPath(Misc.MOD_ID, name));
	}
}
