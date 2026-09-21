package com.frnc.misc.mechanics.mainscreen;

import com.frnc.misc.Misc;
import com.simibubi.create.infrastructure.gui.OpenCreateMenuButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

/**
 * 精简原版主菜单，4 行 1 列布局（移植自 create_void 的 {@code TitleScreenMixin}）：
 * <pre>
 *     单人游戏
 *     多人游戏
 *  [Create] 选项
 *     退出游戏
 * </pre>
 *
 * <p>取消原版 {@code init()} 里添加的 Realms / Mods / 语言 / 辅助功能 / 版权等按钮，
 * 只保留上面 4 行；Create 的按钮由本类手动添加在"选项"行左侧，同时由
 * {@code OpenCreateMenuButtonHandlerMixin} 取消 Create 自身的自动添加，避免重复。
 *
 * <h2>为什么用事件而不是 Mixin</h2>
 * create_void 的做法是 Mixin 掉 {@code TitleScreen.init()} 并在 HEAD 处 {@code ci.cancel()}，
 * 再通过自建的 {@code ScreenInvoker} 调用受保护的 {@code Screen.addRenderableWidget}。
 * 那条路要额外维护一个泛型 {@code @Invoker} 的 refmap（Mixin AP 解析不了泛型方法，
 * 必须在 build.gradle 里手工补 SRG 名），成本高且易碎。
 *
 * <p>Forge 的 {@link ScreenEvent.Init.Post} 提供了公开的
 * {@link ScreenEvent.Init#addListener}/{@link ScreenEvent.Init#removeListener}，
 * 效果与 {@code addRenderableWidget} 完全一致（都会同时进 renderables / children / narratables）。
 * 因此这里保留原版 {@code init()} 正常跑完（Forge 的 {@code modUpdateNotification} 字段
 * 仍然会被正确初始化），在 Post 阶段先移除原版按钮再添加新按钮即可，
 * 既不需要 Mixin，也不会碰到 Forge 无条件调用 {@code modUpdateNotification.render(...)} 的 NPE。
 */
@Mod.EventBusSubscriber(modid = Misc.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TitleScreenLayout {

    private TitleScreenLayout() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen titleScreen)) {
            return;
        }

        // getListenersList() 是不可修改视图，移除按钮会让底层列表变化，
        // 所以先快照再删，避免遍历时并发修改。
        for (GuiEventListener listener : new ArrayList<>(event.getListenersList())) {
            event.removeListener(listener);
        }

        Minecraft minecraft = Minecraft.getInstance();
        int width = titleScreen.width;
        int height = titleScreen.height;

        // 布局基准：行首 y 取 height / 4 + 32，行间距 24，按钮宽 98 水平居中
        int y0 = height / 4 + 32;
        int x = width / 2 - 49;

        // 第 1 行：单人游戏
        event.addListener(Button.builder(Component.translatable("menu.singleplayer"),
                        button -> minecraft.setScreen(new SelectWorldScreen(titleScreen)))
                .bounds(x, y0, 98, 20)
                .build());

        // 第 2 行：多人游戏（沿用原版跳过警告逻辑）
        event.addListener(Button.builder(Component.translatable("menu.multiplayer"), button -> {
                    Screen target = minecraft.options.skipMultiplayerWarning
                            ? new JoinMultiplayerScreen(titleScreen)
                            : new SafetyScreen(titleScreen);
                    minecraft.setScreen(target);
                })
                .bounds(x, y0 + 24, 98, 20)
                .build());

        // 第 3 行：Create 主菜单按钮（选项左侧）+ 选项
        event.addListener(new OpenCreateMenuButton(x - 22, y0 + 48));
        event.addListener(Button.builder(Component.translatable("menu.options"),
                        button -> minecraft.setScreen(new OptionsScreen(titleScreen, minecraft.options)))
                .bounds(x, y0 + 48, 98, 20)
                .build());

        // 第 4 行：退出游戏
        event.addListener(Button.builder(Component.translatable("menu.quit"), button -> minecraft.stop())
                .bounds(x, y0 + 72, 98, 20)
                .build());
    }
}
