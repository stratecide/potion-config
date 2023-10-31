package com.stratecide.potion_config;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stratecide.potion_config.effects.AfterEffect;
import com.stratecide.potion_config.effects.CustomStatusEffect;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomPotion {
    private static final Text NONE_TEXT = Text.translatable("effect.none").formatted(Formatting.GRAY);

    public final Identifier potionId;
    final PotionType type;
    final int duration;
    final boolean isPermanent;
    final Optional<Identifier> afterEffects;
    final List<CustomEffect> effects;
    final PotionColor color;

    public static CustomPotion parse(Identifier potionId, JsonObject jsonObject) {
        PotionType type = PotionType.Normal;
        int duration = PotionConfigMod.DURATION_DEFAULT;
        boolean isPermanent = false;
        PotionColor color = PotionColor.neutral();
        Optional<Identifier> afterEffects = Optional.empty();
        List<CustomEffect> effects = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            switch (key) {
                case "type":
                    type = switch (value.getAsString()) {
                        case "splash" -> PotionType.Splash;
                        case "linger", "lingering" -> PotionType.Lingering;
                        case "craft" -> PotionType.CraftIngredient;
                        default -> PotionType.Normal;
                    };
                    break;
                case "duration":
                    duration = value.getAsInt();
                    break;
                case "permanent":
                    isPermanent = value.getAsBoolean();
                    break;
                case "color":
                    color = PotionColor.parse(value);
                    break;
                case "after":
                    afterEffects = Optional.of(AfterEffect.parse(value.getAsJsonObject()));
                    break;
                default:
                    Identifier identifier = new Identifier(key);
                    if (Registry.STATUS_EFFECT.containsId(identifier)) {
                        StatusEffect effect = Registry.STATUS_EFFECT.get(identifier);
                        effects.add(new CustomEffect(effect, value.getAsJsonObject()));
                    } else {
                        throw new RuntimeException("Unknown status effect " + identifier);
                    }
            }
        }
        return new CustomPotion(potionId, type, duration, isPermanent, color, afterEffects, effects);
    }

    public static CustomPotion empty(Identifier potionId) {
        return new CustomPotion(potionId, PotionType.Normal, 0, false, PotionColor.neutral(), Optional.empty(), new ArrayList<>());
    }

    private CustomPotion(Identifier potionId, PotionType type, int duration, boolean isPermanent, PotionColor color, Optional<Identifier> afterEffects, List<CustomEffect> effects) {
        this.potionId = potionId;
        this.type = type;
        this.duration = duration;
        this.isPermanent = isPermanent;
        this.color = color;
        this.afterEffects = afterEffects;
        this.effects = effects;
    }

    public PotionType getType() {
        return type;
    }

    public Item getPotionItem() {
        return switch (type) {
            case Normal -> Items.POTION;
            case Splash -> Items.SPLASH_POTION;
            case Lingering -> Items.LINGERING_POTION;
            case CraftIngredient -> PotionConfigMod.CRAFTING_POTION;
        };
    }

    public boolean canBeArrow() {
        return type == PotionType.CraftIngredient && PotionConfigMod.ARROW_POTIONS.contains(this);
    }

    public int getColor(boolean inBottle) {
        return color.getColor(inBottle);
    }

    public List<StatusEffectInstance> generateEffectInstances() {
        return generateEffectInstances(false, true, true);
    }

    public List<StatusEffectInstance> generateEffectInstances(boolean ambient, boolean showParticles, boolean showIcon) {
        return generateEffectInstances(effects, duration, afterEffects, ambient, showParticles, showIcon);
    }

    public static List<StatusEffectInstance> generateEffectInstances(List<CustomEffect> effects, int duration, Optional<Identifier> afterEffects, boolean ambient, boolean showParticles, boolean showIcon) {
        List<StatusEffectInstance> result = new ArrayList<>();
        for (CustomEffect effect : effects) {
            if (Math.random() < effect.chance) {
                if (effect.effect instanceof CustomStatusEffect) {
                    ((CustomStatusEffect) effect.effect).addInstances(result, duration, effect.strength - 1, ambient, showParticles, showIcon);
                } else {
                    result.add(new StatusEffectInstance(effect.effect, duration, effect.strength - 1, ambient, showParticles, showIcon && effect.showIcon()));
                }
            }
        }
        if (result.size() > 0 && afterEffects.isPresent()) {
            StatusEffect effect = Registry.STATUS_EFFECT.get(afterEffects.get());
            if (effect instanceof AfterEffect) {
                result.add(new StatusEffectInstance(effect, duration, 0, false, false, false));
            }
        }
        return result;
    }

    public void buildToolTip(List<Text> list, float durationMultiplier) {
        if (this.effects.isEmpty()) {
            list.add(NONE_TEXT);
        } else {
            ArrayList<Pair<EntityAttribute, EntityAttributeModifier>> attributeModifiers = Lists.newArrayList();
            for (CustomEffect effect : effects) {
                if (effect.showIcon() && effect.chance >= PotionConfigMod.HIDE_EFFECTS_BELOW_CHANCE) {
                    effect.buildToolTip(list, attributeModifiers, duration, isPermanent);
                }
            }
            if (!PotionConfigMod.HIDE_AFTER_EFFECTS && afterEffects.isPresent()) {
                list.add(Text.translatable("potion-config.potion.afterEffect").formatted(Formatting.GRAY));
            }
            if (!attributeModifiers.isEmpty()) {
                list.add(ScreenTexts.EMPTY);
                list.add(Text.translatable("potion.whenDrank").formatted(Formatting.DARK_PURPLE));
                for (Pair pair : attributeModifiers) {
                    EntityAttributeModifier entityAttributeModifier = (EntityAttributeModifier)pair.getRight();
                    double d = entityAttributeModifier.getValue();
                    double e = entityAttributeModifier.getOperation() == EntityAttributeModifier.Operation.MULTIPLY_BASE || entityAttributeModifier.getOperation() == EntityAttributeModifier.Operation.MULTIPLY_TOTAL ? entityAttributeModifier.getValue() * 100.0 : entityAttributeModifier.getValue();
                    if (d > 0.0) {
                        list.add(Text.translatable("attribute.modifier.plus." + entityAttributeModifier.getOperation().getId(), ItemStack.MODIFIER_FORMAT.format(e), Text.translatable(((EntityAttribute)pair.getLeft()).getTranslationKey())).formatted(Formatting.BLUE));
                    } else if (d < 0.0) {
                        list.add(Text.translatable("attribute.modifier.take." + entityAttributeModifier.getOperation().getId(), ItemStack.MODIFIER_FORMAT.format(e *= -1.0), Text.translatable(((EntityAttribute) pair.getLeft()).getTranslationKey())).formatted(Formatting.RED));
                    }
                }
            }
        }
    }
}
