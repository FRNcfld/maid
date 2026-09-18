package com.frnc.misc.mechanics.recall_potion;

import com.frnc.misc.Misc;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 物品注册持有类:「回归药水」(misc:recall_potion)。
 */
public final class RecallPotionItems {

	public static final DeferredRegister<Item> ITEMS =
			DeferredRegister.create(ForgeRegistries.ITEMS, Misc.MOD_ID);

	public static final RegistryObject<RecallPotionItem> RECALL_POTION = ITEMS.register(
			"recall_potion",
			() -> new RecallPotionItem(new Item.Properties()
					.craftRemainder(Items.GLASS_BOTTLE)
					.stacksTo(16)));

	private RecallPotionItems() {
	}
}
