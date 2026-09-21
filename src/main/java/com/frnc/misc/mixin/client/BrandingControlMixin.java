package com.frnc.misc.mixin.client;

import net.minecraftforge.internal.BrandingControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

/**
 * 移除标题屏的品牌水印（精简页面）。移植自 create_void 的同名 Mixin。
 *
 * <p>Forge 1.20.1 的版本/模组水印由 {@code BrandingControl.forEachLine(...)} 绘制（左下角：
 * Forge 版本 / Minecraft 版本 / MCP / 模组数量），右下角的 Forge 状态行由
 * {@code forEachAboveCopyrightLine(...)} 绘制。两个都置为 no-op。
 *
 * <p>注意：{@code TitleScreen.render} 用 {@code forEachLine(true, true, ...)} 画左下角、
 * 用 {@code forEachAboveCopyrightLine(...)} 画右下角，两处都会被这里拦掉；
 * 原版版权行（{@code TitleScreen.COPYRIGHT_TEXT}）是 {@code PlainTextButton}，
 * 由 {@code TitleScreenLayout} 在初始化后移除，不经过本 Mixin。
 *
 * <p>{@code BrandingControl} 是 Forge 自身的类，不能被混淆，所以 {@code remap = false}。
 */
@Mixin(value = BrandingControl.class, remap = false)
public abstract class BrandingControlMixin {

    @Inject(method = "forEachLine", at = @At("HEAD"), cancellable = true, remap = false)
    private static void misc$noLeftBranding(boolean includeMC, boolean reverse,
                                            BiConsumer<Integer, String> lineConsumer, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "forEachAboveCopyrightLine", at = @At("HEAD"), cancellable = true, remap = false)
    private static void misc$noRightBranding(BiConsumer<Integer, String> lineConsumer, CallbackInfo ci) {
        ci.cancel();
    }
}
