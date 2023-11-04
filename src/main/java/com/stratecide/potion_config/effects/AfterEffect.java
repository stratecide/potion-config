package com.stratecide.potion_config.effects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stratecide.potion_config.*;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AfterEffect extends CustomStatusEffect {

    public static final String AFTER_EFFECT_PREFIX = "after_effect_";
    private static int NEXT_ID = 0;

    final int duration;
    public final List<CustomEffect> effects;

    AfterEffect(int duration, List<CustomEffect> effects) {
        super(StatusEffectCategory.NEUTRAL, 0x00000000);
        this.duration = duration;
        this.effects = effects;
    }

    public static Identifier parse(JsonObject jsonObject) {
        int duration = -1;
        List<CustomEffect> effects = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            switch (key) {
                case "duration":
                    duration = value.getAsInt();
                    break;
                default:
                    Identifier identifier = new Identifier(key);
                    if (Registries.STATUS_EFFECT.containsId(identifier)) {
                        StatusEffect effect = Registries.STATUS_EFFECT.get(identifier);
                        effects.add(new CustomEffect(effect, value.getAsJsonObject()));
                    } else {
                        throw new RuntimeException("Unknown status effect " + identifier);
                    }
            }
        }
        Identifier identifier = new Identifier(PotionConfigMod.MOD_ID, AFTER_EFFECT_PREFIX + NEXT_ID);
        if (duration < 0) {
            throw new RuntimeException("Missing duration for after-effect " + identifier);
        }
        NEXT_ID += 1;
        Registry.register(Registries.STATUS_EFFECT, identifier, new AfterEffect(duration, effects));
        return identifier;
    }

    public List<StatusEffectInstance> generateEffectInstances() {
        return CustomPotion.generateEffectInstances(effects, duration, Optional.empty(), false, true, true);
    }
}
