package com.stratecide.potion_config;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArrowRecipe {
    final Pattern inputPattern;
    final Optional<PotionType> inputType;
    final String outputPattern;

    public ArrowRecipe(Pattern inputPattern, Optional<PotionType> inputType, String outputPattern) {
        this.inputPattern = inputPattern;
        this.inputType = inputType;
        this.outputPattern = outputPattern;
    }

    public boolean matches(PotionType inputType, Potion potion) {
        String customId = PotionConfigMod.getCustomPotionId(inputType, potion);
        return matches(inputType, customId);
    }
    public boolean matches(PotionType inputType, String customId) {
        if (this.inputType.isPresent() && !inputType.equals(this.inputType.get())) {
            return false;
        }
        Matcher matcher = this.inputPattern.matcher(customId);
        if (customId == null || !matcher.matches()) {
            return false;
        }
        String outputPattern = this.outputPattern;
        for (int i = 1; i <= matcher.groupCount(); i++) {
            outputPattern = outputPattern.replace("(" + i + ")", matcher.group(i));
        }
        return PotionConfigMod.hasCustomArrowPotion(outputPattern);
    }

    public ItemStack craft(PotionType inputType, Potion input) {
        String customId = PotionConfigMod.getCustomPotionId(inputType, input);
        Matcher matcher = this.inputPattern.matcher(customId);
        matcher.matches();
        String outputPattern = this.outputPattern;
        for (int i = 1; i <= matcher.groupCount(); i++) {
            outputPattern = outputPattern.replace("(" + i + ")", matcher.group(i));
        }
        Potion potion = PotionConfigMod.getOriginalArrowPotion(outputPattern);
        ItemStack itemStack = new ItemStack(Items.TIPPED_ARROW, 8);
        PotionUtil.setPotion(itemStack, potion);
        return itemStack;
    }
}
