package com.stratecide.potion_config.effects;

import com.stratecide.potion_config.PotionColor;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class CustomStatusEffect extends StatusEffect {
    protected CustomStatusEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    public void onReapplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        this.onRemoved(entity, attributes, amplifier);
        this.onApplied(entity, attributes, amplifier);
    }

    public static boolean showIcon(StatusEffect effect) {
        return !(effect instanceof AfterEffect)
            && !(effect instanceof Particles);
    }

    public void addInstances(List<StatusEffectInstance> result, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
        result.add(new StatusEffectInstance(this, duration, amplifier, ambient, showParticles, showIcon && CustomStatusEffect.showIcon(this)));
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        this.onRemoved1(entity, attributes, amplifier);
        this.onRemoved2(entity);
    }

    public void onRemoved1(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
    }
    public void onRemoved2(LivingEntity entity) {
    }

    public boolean buildToolTip(List<Text> list, double chance, int duration, int strength, boolean isPermanent) {
        return false;
    }

    public static final CustomStatusEffect HEALTH_BOOST = register("health_boost", new HealthBoost(StatusEffectCategory.BENEFICIAL, 0xf87d23));
    public static final CustomStatusEffect HEALTH_DROP = register("health_drop", new HealthDrop(StatusEffectCategory.HARMFUL, 0x5a3740));
    public static final StatusEffect JUMP_DROP = register("jump_drop", new CustomStatusEffect(StatusEffectCategory.HARMFUL, 2293580));
    public static final CustomStatusEffect PARTICLES = register("particles", new Particles(StatusEffectCategory.NEUTRAL, 0, PotionColor.neutral()));
    public static final CustomStatusEffect RANDOM_CHOICE = register("random_choice", new RandomChoice(StatusEffectCategory.NEUTRAL, 0, new ArrayList()));
    public static final CustomStatusEffect ALL_OR_NONE = register("all_or_none", new AllOrNone(StatusEffectCategory.NEUTRAL, 0, new ArrayList()));
    public static final CustomStatusEffect CREATIVE_FLIGHT = register("creative_flight", new CreativeFlight(StatusEffectCategory.BENEFICIAL, 0x66bbff));
    public static final CustomStatusEffect KNOCKBACK_RESISTANCE = register("knockback_resistance", (CustomStatusEffect) new CustomStatusEffect(StatusEffectCategory.BENEFICIAL, 0x66bbff).addAttributeModifier(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, "4401de28-ce2f-4c3b-85f5-89f6885de58f", 0.05, EntityAttributeModifier.Operation.ADDITION));
    public static final CustomStatusEffect FLAMES = register("flames", new Flames(StatusEffectCategory.HARMFUL, 0xff6600));
    public static final CustomStatusEffect MILK = register("milk", new Milk(StatusEffectCategory.NEUTRAL, 0xffffff));
    // mine better when in bad terrain (e.g. under water, flying, on ladders)
    public static final CustomStatusEffect FINESSE = register("finesse", new CustomStatusEffect(StatusEffectCategory.BENEFICIAL, 0x9999aa));
    // moves camera in random directions, may change main-hand slot as if the scroll-wheel was used
    public static final CustomStatusEffect DRUNK = register("drunk", new Drunk(StatusEffectCategory.HARMFUL, 0x448822));
    private static CustomStatusEffect register(String id, CustomStatusEffect entry) {
        return Registry.register(Registry.STATUS_EFFECT, new Identifier(PotionConfigMod.MOD_ID, id), entry);
    }
}
