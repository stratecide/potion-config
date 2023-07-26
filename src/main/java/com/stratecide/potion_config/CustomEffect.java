package com.stratecide.potion_config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.stratecide.potion_config.effects.AllOrNone;
import com.stratecide.potion_config.effects.CustomStatusEffect;
import com.stratecide.potion_config.effects.Particles;
import com.stratecide.potion_config.effects.RandomChoice;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomEffect {
    public final StatusEffect effect;
    public final double chance;
    public final int strength;

    public CustomEffect(StatusEffect effect, JsonObject jsonObject) {
        if (effect instanceof Particles) {
            PotionColor color = PotionColor.parse(jsonObject.get("color"));
            effect = ((Particles) effect).withColor(color);
        } else if (effect instanceof RandomChoice) {
            List<CustomEffect> effects = new ArrayList<>();
            for (JsonElement option : jsonObject.get("options").getAsJsonArray()) {
                JsonObject opt = option.getAsJsonObject();
                Identifier identifier = new Identifier(opt.get("key").getAsString());
                if (Registries.STATUS_EFFECT.containsId(identifier)) {
                    StatusEffect eff = Registries.STATUS_EFFECT.get(identifier);
                    effects.add(new CustomEffect(eff, opt));
                } else {
                    throw new RuntimeException("Unknown status effect " + identifier);
                }
            }
            effect = ((RandomChoice) effect).withOptions(effects);
        } else if (effect instanceof AllOrNone) {
            List<CustomEffect> effects = new ArrayList<>();
            for (JsonElement option : jsonObject.get("children").getAsJsonArray()) {
                JsonObject opt = option.getAsJsonObject();
                Identifier identifier = new Identifier(opt.get("key").getAsString());
                if (Registries.STATUS_EFFECT.containsId(identifier)) {
                    StatusEffect eff = Registries.STATUS_EFFECT.get(identifier);
                    opt.addProperty("chance", 1.0);
                    effects.add(new CustomEffect(eff, opt));
                } else {
                    throw new RuntimeException("Unknown status effect " + identifier);
                }
            }
            effect = ((AllOrNone) effect).withChildren(effects);
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
        if (effect instanceof CustomStatusEffect && ((CustomStatusEffect) effect).buildToolTip(list, chance, duration, strength, isPermanent)) {
            return;
        }
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
}
