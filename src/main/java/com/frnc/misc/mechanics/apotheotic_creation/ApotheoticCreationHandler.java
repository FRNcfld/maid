package com.frnc.misc.mechanics.apotheotic_creation;

import com.frnc.misc.Misc;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 机械动力 (Create) 过滤器识别神化 (Apotheosis) 装备属性。
 *
 * 向 CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE 注册两个新属性类型:
 *   - {modid}:rarity  识别装备的 Apotheosis 稀有度 (LootRarity)
 *   - {modid}:affix   识别装备是否带有某个 Apotheosis 词缀 (Affix)
 */
@Mod.EventBusSubscriber(modid = Misc.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ApotheoticCreationHandler
{
    private static final ResourceLocation RARITY_ID = ResourceLocation.fromNamespaceAndPath(Misc.MOD_ID, "rarity");
    private static final ResourceLocation AFFIX_ID = ResourceLocation.fromNamespaceAndPath(Misc.MOD_ID, "affix");

    @SubscribeEvent
    public static void onRegister(final RegisterEvent event)
    {
        var attributeRegistryKey = CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.key();
        if (event.getRegistryKey() == attributeRegistryKey)
        {
            event.register(attributeRegistryKey, RARITY_ID, RarityAttribute.Type::new);
            event.register(attributeRegistryKey, AFFIX_ID, AffixAttribute.Type::new);
        }
    }

    /** 稀有度属性: 匹配装备的 Apotheosis 稀有度 */
    public static class RarityAttribute implements ItemAttribute
    {
        private LootRarity rarity;

        public RarityAttribute(LootRarity rarity)
        {
            this.rarity = rarity;
        }

        @Override
        public boolean appliesTo(ItemStack stack, Level level)
        {
            DynamicHolder<LootRarity> itemRarity = AffixHelper.getRarity(stack);
            if (!itemRarity.isBound()) return false;
            return itemRarity.get() == this.rarity;
        }

        @Override
        public String getTranslationKey()
        {
            return "item_rarity";
        }

        @Override
        public Object[] getTranslationParameters()
        {
            return this.rarity != null ? new Object[]{this.rarity.toComponent()} : new Object[0];
        }

        @Override
        public void save(CompoundTag nbt)
        {
            if (this.rarity != null) nbt.putInt("rarity", this.rarity.ordinal());
        }

        @Override
        public void load(CompoundTag nbt)
        {
            if (nbt.contains("rarity"))
            {
                DynamicHolder<LootRarity> holder = RarityRegistry.byOrdinal(nbt.getInt("rarity"));
                if (holder.isBound()) this.rarity = holder.get();
            }
        }

        @Override
        public ItemAttributeType getType()
        {
            return CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.get(RARITY_ID);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof RarityAttribute that)) return false;
            return Objects.equals(this.rarity, that.rarity);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(this.rarity);
        }

        public static class Type implements ItemAttributeType
        {
            @Override
            public ItemAttribute createAttribute()
            {
                return new RarityAttribute(null);
            }

            @Override
            public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level)
            {
                DynamicHolder<LootRarity> itemRarity = AffixHelper.getRarity(stack);
                if (!itemRarity.isBound()) return Collections.emptyList();
                return List.of(new RarityAttribute(itemRarity.get()));
            }
        }
    }

    /** 词缀属性: 匹配装备是否带有某个 Apotheosis 词缀 */
    public static class AffixAttribute implements ItemAttribute
    {
        private static final Set<String> HIDDEN_AFFIXES = Set.of("socket", "durable");

        private DynamicHolder<? extends Affix> affix;

        public AffixAttribute(DynamicHolder<? extends Affix> affix)
        {
            this.affix = affix;
        }

        @Override
        public boolean appliesTo(ItemStack stack, Level level)
        {
            Map<DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
            return affixes.containsKey(this.affix);
        }

        @Override
        public String getTranslationKey()
        {
            return "item_affix";
        }

        @Override
        public Object[] getTranslationParameters()
        {
            if (this.affix != null && this.affix.getId() != null)
            {
                return new Object[]{Component.translatable("affix." + this.affix.getId())};
            }
            return new Object[0];
        }

        @Override
        public void save(CompoundTag nbt)
        {
            if (this.affix != null && this.affix.getId() != null)
            {
                nbt.putString("affix_namespace", this.affix.getId().getNamespace());
                nbt.putString("affix_path", this.affix.getId().getPath());
            }
        }

        @Override
        public void load(CompoundTag nbt)
        {
            if (nbt.contains("affix_namespace") && nbt.contains("affix_path"))
            {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                        nbt.getString("affix_namespace"), nbt.getString("affix_path"));
                DynamicHolder<? extends Affix> holder = AffixRegistry.INSTANCE.holder(id);
                if (holder.isBound()) this.affix = holder;
            }
        }

        @Override
        public ItemAttributeType getType()
        {
            return CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.get(AFFIX_ID);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof AffixAttribute that)) return false;
            return Objects.equals(this.affix, that.affix);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(this.affix);
        }

        public static class Type implements ItemAttributeType
        {
            @Override
            public ItemAttribute createAttribute()
            {
                return new AffixAttribute(null);
            }

            @Override
            public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level)
            {
                return AffixHelper.getAffixes(stack).keySet().stream()
                        .filter(entry -> entry.isBound() && !HIDDEN_AFFIXES.contains(entry.getId().getPath()))
                        .map(AffixAttribute::new)
                        .collect(Collectors.toList());
            }
        }
    }
}
