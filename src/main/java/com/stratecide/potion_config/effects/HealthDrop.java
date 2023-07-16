package com.stratecide.potion_config.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;

public class HealthDrop extends CustomStatusEffect {
    public HealthDrop(StatusEffectCategory statusEffectCategory, int i) {
        super(statusEffectCategory, i);
        this.addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, "65f775dc-4eda-4798-89cf-dbc344f23bf1", -2.0, EntityAttributeModifier.Operation.ADDITION);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }
}
