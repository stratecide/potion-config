package com.stratecide.potion_config.effects;

import com.google.common.collect.Lists;
import com.stratecide.potion_config.CustomEffect;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoveEffect extends CustomStatusEffect {
    private static final Map<StatusEffect, RemoveEffect> CREATED = new HashMap<>();

    public static Identifier generateIdentifier(long index) {
        String path = "remove_effect";
        if (index > 0)
            path += index;
        return new Identifier(PotionConfigMod.MOD_ID, path);
    }

    private final StatusEffect removedEffect;

    private RemoveEffect(StatusEffect removedEffect) {
        super(switch (removedEffect.getCategory()) {
            case HARMFUL -> StatusEffectCategory.BENEFICIAL;
            case NEUTRAL -> StatusEffectCategory.NEUTRAL;
            case BENEFICIAL -> StatusEffectCategory.HARMFUL;
        }, 0);
        this.removedEffect = removedEffect;
    }

    public static RemoveEffect getOrCreate(StatusEffect removedEffect) {
        RemoveEffect result = CREATED.get(removedEffect);
        if (result == null) {
            int id = CREATED.size();
            result = Registry.register(Registry.STATUS_EFFECT, generateIdentifier(id), new RemoveEffect(removedEffect));
            CREATED.put(removedEffect, result);
        }
        return result;
    }

    @Override
    public boolean isInstant() {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.world.isClient) {
            entity.removeStatusEffect(removedEffect);
        }
    }

    @Override
    public boolean buildToolTip(List<Text> list, double chance, int duration, int strength, boolean isPermanent) {
        Text statusEffect = Text.translatable(this.removedEffect.getTranslationKey());
        list.add(Text.translatable("effect.potion-config.remove_effect", statusEffect).formatted(getCategory().getFormatting()));
        return true;
    }
}
