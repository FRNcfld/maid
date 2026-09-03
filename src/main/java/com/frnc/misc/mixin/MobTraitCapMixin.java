package com.frnc.misc.mixin;

import com.frnc.misc.world.overworld.l2hostility.NoHostilityCap;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 为 L2Hostility 的 {@link MobTraitCap} 增加"0 级初始化"能力。
 *
 * 配合 {@link com.frnc.misc.world.overworld.l2hostility.OverworldNoHostilityHandler}:
 * 主世界自然生成 (MobSpawnType.NATURAL) 的非孢子生物, 在 L2Hostility 为其定级之前,
 * 先把 cap 置为已初始化 (stage = INIT) 且等级 lv = 0。L2Hostility 看到 isInitialized()
 * 为 true 便不会再进行等级/词条初始化, 生物保持原版强度 (无恶意等级)。
 *
 * 这是只读外的"写入门": 原类为第三方模组类 (dev.xkmc.*), 运行时不做混淆重映射,
 * 故对字段的 Shadow / 对方法的引用一律 remap = false。
 */
@Mixin(MobTraitCap.class)
public abstract class MobTraitCapMixin implements NoHostilityCap
{
    @Shadow(remap = false)
    public int lv;

    @Shadow(remap = false)
    private MobTraitCap.Stage stage;

    @Override
    public void misc$suppressHostility()
    {
        this.lv = 0;
        this.stage = MobTraitCap.Stage.INIT;
    }
}
