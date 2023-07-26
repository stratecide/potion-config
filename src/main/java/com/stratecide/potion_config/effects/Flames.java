package com.stratecide.potion_config.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;

public class Flames extends CustomStatusEffect {
    protected Flames(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    public boolean isInstant() {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.getWorld().isClient && !entity.isFireImmune() && !entity.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            entity.setOnFireFor(1 + amplifier);
        }
    }
}
