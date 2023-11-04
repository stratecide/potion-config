package com.stratecide.potion_config.effects;

import com.google.common.collect.Lists;
import com.stratecide.potion_config.CustomEffect;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RandomChoice extends CustomStatusEffect {
    private static int ID = 0;
    public final List<CustomEffect> options;
    public final int id;

    public static Identifier generateIdentifier(long index) {
        return new Identifier(PotionConfigMod.MOD_ID, "random_choice" + index);
    }

    protected RandomChoice(StatusEffectCategory statusEffectCategory, int id, List<CustomEffect> options) {
        super(statusEffectCategory, 0);
        this.id = id;
        this.options = options.stream().filter(customEffect -> customEffect.chance > 0).collect(Collectors.toList());
    }

    public RandomChoice withOptions(List<CustomEffect> options) {
        boolean isBeneficial = options.stream().anyMatch(customEffect -> customEffect.effect.isBeneficial());
        boolean isHarmful = options.stream().anyMatch(customEffect -> StatusEffectCategory.HARMFUL.equals(customEffect.effect.getCategory()));
        StatusEffectCategory category = (isBeneficial == isHarmful) ? StatusEffectCategory.NEUTRAL : isBeneficial ? StatusEffectCategory.BENEFICIAL : StatusEffectCategory.HARMFUL;
        int id = ++ID;
        return Registry.register(Registries.STATUS_EFFECT, generateIdentifier(id), new RandomChoice(category, id, options));
    }

    public boolean isInstant() {
        return true;
    }

    @Override
    public void addInstances(List<StatusEffectInstance> result, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
        List<CustomEffect> options = new ArrayList<>(this.options);
        int added = 0;
        while (options.size() > 0 && added < amplifier + 1) {
            added += 1;
            double totalChance = options.stream().map(effect -> effect.chance).mapToDouble(Double::doubleValue).sum();
            double random = Math.random() * totalChance;
            for (int i = 0; i < options.size(); i++) {
                CustomEffect option = options.get(i);
                random -= option.chance;
                if (random < 0) {
                    // the same effect shouldn't be added multiple times
                    options.remove(i);
                    if (option.effect instanceof CustomStatusEffect) {
                        ((CustomStatusEffect) option.effect).addInstances(result, duration, option.strength - 1, ambient, showParticles, showIcon);
                    } else {
                        result.add(new StatusEffectInstance(option.effect, duration, option.strength - 1, ambient, showParticles, showIcon && option.showIcon()));
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean buildToolTip(List<Text> list, double chance, int duration, int strength, boolean isPermanent) {
        MutableText mutableText = Text.translatable("effect.potion-config.random_choice", strength);
        if (chance < 1.) {
            mutableText = Text.translatable("potion-config.potion.withChance", mutableText, ((int) Math.round(chance * 100)));
        }
        list.add(mutableText.formatted(Formatting.GRAY));
        ArrayList<Pair<EntityAttribute, EntityAttributeModifier>> attributeModifiers = Lists.newArrayList();
        List<Text> children = new ArrayList<>();
        for (CustomEffect effect : options) {
            if (effect.showIcon() && effect.chance >= PotionConfigMod.HIDE_EFFECTS_BELOW_CHANCE) {
                effect.buildToolTip(children, attributeModifiers, duration, isPermanent);
            }
        }
        for (Text child : children) {
            list.add(Text.translatable("potion-config.indent", child));
        }
        return true;
    }
}
