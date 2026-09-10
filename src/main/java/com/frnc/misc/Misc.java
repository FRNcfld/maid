package com.frnc.misc;

import com.frnc.misc.mechanics.doublejump.JumpHandler;
import com.frnc.misc.mechanics.doublejump.network.DoubleJumpNetwork;
import com.frnc.misc.mechanics.elytraflight.network.ElytraFlightNetwork;
import com.frnc.misc.mechanics.CreateModification.CreateConfigHandler;
import com.frnc.misc.mechanics.apotheotic_l2hostility.ApotheoticL2HostilityConfigHandler;
import com.frnc.misc.mechanics.kaleidoscope_cookery.KaleidoscopeConfigHandler;
import com.frnc.misc.mechanics.liquidburner.RecipeRegistry;
import com.frnc.misc.mechanics.timid_curse.TimidCurseItems;
import com.mojang.logging.LogUtils;
import dev.xkmc.l2hostility.init.registrate.LHBlocks;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Misc.MOD_ID)
public class Misc
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "misc";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public Misc(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register 「怯懦诅咒」(Curse of Timid) Curio 饰品
        TimidCurseItems.ITEMS.register(modEventBus);

        // Register liquidburning 配方类型与序列化器 (Create 烈焰人燃烧器液体燃料)
        RecipeRegistry.register(modEventBus);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Register the double jump network channel and packets
        DoubleJumpNetwork.register();

        // Register the elytra flight switch network channel and packets
        ElytraFlightNetwork.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        // Initialize the double jump default state from config
        JumpHandler.initFromConfig();

        // Override the Satiated Shield default config (runs after all mod configs are loaded)
        KaleidoscopeConfigHandler.applyOverrides();

        // Allow L2Hostility to level No-AI mobs (Apotheosis chorus-fruit spawners)
        ApotheoticL2HostilityConfigHandler.applyOverrides();
    }

    // Add the frnc block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        // 将「怯懦诅咒」挂靠到莱特兰·恶意(L2 Hostility)的创造标签页
        if (event.getTab() == LHBlocks.TAB.get())
        {
            event.accept(TimidCurseItems.CURSE_OF_TIMID.get());
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");

        // 覆写 Create 锁链传送带最大连接长度 (32 -> 512):
        // 该值位于 SERVER 配置, 进服时才加载, 故不能在 commonSetup 调用; ServerStarting 晚于配置加载
        CreateConfigHandler.applyOverrides();
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
