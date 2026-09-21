package com.frnc.misc.mixin.client;

import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 移除主标题界面（Minecraft 标题屏）的黄色闪烁标语（splash）。
 *
 * <p>原版 {@code TitleScreen.init()} 会在 {@code this.splash == null} 时调用
 * {@code Minecraft.getSplashManager().getSplash()} 取一条标语，{@code render()} 里再判断
 * {@code if (this.splash != null)} 才绘制。这里直接在取标语这一步返回 null，
 * 于是 {@code TitleScreen.splash} 永远为 null，整条黄色文字（含圣诞节/新年/万圣节特殊标语）
 * 都不会出现，且不需要改动任何资源文件。
 *
 * <p>{@code getSplash()} 标注为 {@code @Nullable}，返回 null 是合法取值；
 * 该方法在 1.20.1 中只有 {@code TitleScreen} 一个调用方，因此影响面仅限于标题屏。
 */
@Mixin(SplashManager.class)
public abstract class SplashManagerMixin {

    @Inject(method = "getSplash", at = @At("HEAD"), cancellable = true)
    private void misc$noSplash(CallbackInfoReturnable<SplashRenderer> cir) {
        cir.setReturnValue(null);
    }
}
