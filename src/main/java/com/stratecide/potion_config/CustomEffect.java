package com.stratecide.potion_config;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.stratecide.potion_config.effects.CustomStatusEffect;
import com.stratecide.potion_config.effects.Particles;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomEffect {
    final StatusEffect effect;
    final double chance;
    final int strength;

    public CustomEffect(StatusEffect effect, JsonObject jsonObject) {
        if (effect instanceof Particles) {
            PotionColor color = PotionColor.parse(jsonObject.get("color"));
            effect = ((Particles) effect).withColor(color);
        }
        this.effect = effect;
        if (jsonObject.has("chance")) {
            this.chance = jsonObject.get("chance").getAsDouble();
        } else {
            this.chance = 1.;
        }
        int strength = 1;
        if (jsonObject.has("amplifier")) {
            strength += jsonObject.get("amplifier").getAsInt();
        }
        this.strength = strength;
    }

    public boolean showIcon() {
        return CustomStatusEffect.showIcon(effect);
    }

    public void buildToolTip(List<Text> list, ArrayList<Pair<EntityAttribute, EntityAttributeModifier>> attributeModifiers, int duration, boolean isPermanent) {
        MutableText mutableText = Text.translatable(effect.getTranslationKey());
        Map<EntityAttribute, EntityAttributeModifier> map = effect.getAttributeModifiers();
        if (!map.isEmpty()) {
            for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : map.entrySet()) {
                EntityAttributeModifier entityAttributeModifier = entry.getValue();
                EntityAttributeModifier entityAttributeModifier2 = new EntityAttributeModifier(entityAttributeModifier.getName(), effect.adjustModifierAmount(strength - 1, entityAttributeModifier), entityAttributeModifier.getOperation());
                attributeModifiers.add(new Pair<>(entry.getKey(), entityAttributeModifier2));
            }
        }
        if (strength > 10) {
            mutableText = Text.translatable("potion.withAmplifier", mutableText, "" + strength);
        } else if (strength > 1) {
            mutableText = Text.translatable("potion.withAmplifier", mutableText, Text.translatable("enchantment.level." + strength));
        }
        if (!effect.isInstant() && (isPermanent || duration > 20)) {
            String durationString = "**:**";
            if (!isPermanent) {
                durationString = StringHelper.formatTicks(duration);
            }
            mutableText = Text.translatable("potion.withDuration", mutableText, durationString);
        }
        if (chance < 1.) {
            mutableText = Text.translatable("potion-config.potion.withChance", mutableText, ((int) Math.round(chance * 100)));
        }
        list.add(mutableText.formatted(effect.getCategory().getFormatting()));
    }

    interface StatusEffectInstanceProvider {
        public StatusEffectInstance get(int duration, boolean ambient, boolean showParticles, boolean showIcon);
    }
}
