package com.stratecide.potion_config.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;

public class HealthBoost extends CustomStatusEffect {
    public HealthBoost(StatusEffectCategory statusEffectCategory, int i) {
        super(statusEffectCategory, i);
        this.addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, "b333d754-1941-47f8-89f1-ccffc68dd79a", 2.0, EntityAttributeModifier.Operation.ADDITION);
    }

    @Override
    public void onReapplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
        onApplied(entity, attributes, amplifier);
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    @Override
    public void onRemoved2(LivingEntity entity) {
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }
}
