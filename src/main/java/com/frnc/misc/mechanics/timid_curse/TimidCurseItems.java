package com.frnc.misc.mechanics.timid_curse;

import com.frnc.misc.Misc;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 物品注册持有类:「怯懦诅咒」(misc:curse_of_timid)。
 */
public final class TimidCurseItems {

	public static final DeferredRegister<Item> ITEMS =
			DeferredRegister.create(ForgeRegistries.ITEMS, Misc.MOD_ID);

	public static final RegistryObject<TimidCurseItem> CURSE_OF_TIMID = ITEMS.register(
			"curse_of_timid",
			() -> new TimidCurseItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

	private TimidCurseItems() {
	}
}
