package com.frnc.misc.world.overworld.l2hostility;

/**
 * 由 {@link com.frnc.misc.mixin.MobTraitCapMixin} 织入 L2Hostility 的 MobTraitCap, 供外部以
 * {@code (NoHostilityCap)(Object) cap} 形式调用的扩展方法。
 *
 * 作用: 把某个生物的姿态等级"锁零" —— 置 lv = 0 并把 stage 标记为 INIT,
 * 使 L2Hostility 认为该生物已完成初始化。之后无论是刷怪瞬间、EntityJoin
 * 补初始化还是 tick 惰性初始化, 都不会再给它分配恶意等级/词条。
 *
 * 注: 实现该接口的 mixin (com.frnc.misc.mixin.MobTraitCapMixin) 必须留在
 * com.frnc.misc.mixin 包内 —— misc.mixins.json 的 "package" 指向该包, mixin
 * 条目按该包相对解析。
 */
public interface NoHostilityCap
{
    /** 使该生物的 MobTraitCap 以 0 级空词条完成初始化 */
    void misc$suppressHostility();
}
