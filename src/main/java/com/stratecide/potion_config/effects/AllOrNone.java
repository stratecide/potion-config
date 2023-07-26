package com.stratecide.potion_config.effects;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
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

import java.util.ArrayList;
import java.util.List;

public class AllOrNone extends CustomStatusEffect {
    private static int ID = 0;
    public final List<CustomEffect> children;
    public final int id;

    public static Identifier generateIdentifier(long index) {
        return new Identifier(PotionConfigMod.MOD_ID, "all_or_none" + index);
    }

    protected AllOrNone(StatusEffectCategory statusEffectCategory, int id, List<CustomEffect> children) {
        super(statusEffectCategory, 0);
        this.id = id;
        this.children = children;
    }

    public AllOrNone withChildren(List<CustomEffect> children) {
        boolean isBeneficial = children.stream().anyMatch(customEffect -> customEffect.effect.isBeneficial());
        boolean isHarmful = children.stream().anyMatch(customEffect -> StatusEffectCategory.HARMFUL.equals(customEffect.effect.getCategory()));
        StatusEffectCategory category = (isBeneficial == isHarmful) ? StatusEffectCategory.NEUTRAL : isBeneficial ? StatusEffectCategory.BENEFICIAL : StatusEffectCategory.HARMFUL;
        int id = ++ID;
        return Registry.register(Registries.STATUS_EFFECT, generateIdentifier(id), new AllOrNone(category, id, children));
    }

    public boolean isInstant() {
        return true;
    }

    @Override
    public void addInstances(List<StatusEffectInstance> result, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
        for (CustomEffect child : children) {
            if (child.effect instanceof CustomStatusEffect) {
                ((CustomStatusEffect) child.effect).addInstances(result, duration, child.strength - 1, ambient, showParticles, showIcon);
            } else {
                result.add(new StatusEffectInstance(child.effect, duration, child.strength - 1, ambient, showParticles, showIcon && child.showIcon()));
            }
            break;
        }
    }

    @Override
    public boolean buildToolTip(List<Text> list, double chance, int duration, int strength, boolean isPermanent) {
        MutableText mutableText = Text.translatable("effect.potion-config.all_or_none", strength);
        if (chance < 1.) {
            mutableText = Text.translatable("potion-config.potion.withChance", mutableText, ((int) Math.round(chance * 100)));
        }
        list.add(mutableText.formatted(Formatting.GRAY));
        ArrayList<Pair<EntityAttribute, EntityAttributeModifier>> attributeModifiers = Lists.newArrayList();
        List<Text> children = new ArrayList<>();
        for (CustomEffect effect : this.children) {
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
