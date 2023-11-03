package com.stratecide.potion_config.effects;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
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
}
