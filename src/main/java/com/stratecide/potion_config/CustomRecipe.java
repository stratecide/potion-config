package com.stratecide.potion_config;

import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomRecipe {
    final Pattern input;
    final Optional<PotionType> inputType;
    final Ingredient ingredient;
    final List<Output> outputs;

    public CustomRecipe(Pattern input, Optional<PotionType> inputType, Ingredient ingredient, List<Output> outputs) {
        this.input = input;
        this.inputType = inputType;
        this.ingredient = ingredient;
        this.outputs = outputs;
    }

    public boolean matches(PotionType inputType, Potion potion, ItemStack ingredient) {
        String customId = PotionConfigMod.getCustomPotionId(inputType, potion);
        return matches(inputType, customId, ingredient);
    }
    public boolean matches(PotionType inputType, String customId, ItemStack ingredient) {
        if (this.inputType.isPresent() && !inputType.equals(this.inputType.get())) {
            return false;
        }
        if (!this.ingredient.test(ingredient)) {
            return false;
        }
        Matcher matcher = this.input.matcher(customId);
        if (!matcher.matches()) {
            return false;
        }
        for (Output output : outputs) {
            String outputPattern = output.outputPattern;
            for (int i = 1; i <= matcher.groupCount(); i++) {
                outputPattern = outputPattern.replace("(" + i + ")", matcher.group(i));
            }
            PotionType outputType = inputType;
            if (output.outputType.isPresent()) {
                outputType = output.outputType.get();
            }
            if (PotionConfigMod.hasCustomPotion(outputPattern, Optional.of(outputType))) {
                return true;
            }
        }
        return false;
    }

    public List<Result> craftOptions(PotionType inputType, Potion potion) {
        String customId = PotionConfigMod.getCustomPotionId(inputType, potion);
        Matcher matcher = this.input.matcher(customId);
        matcher.matches();
        List<Result> results = new ArrayList<>();
        for (Output output : outputs) {
            String outputPattern = output.outputPattern;
            for (int i = 1; i <= matcher.groupCount(); i++) {
                outputPattern = outputPattern.replace("(" + i + ")", matcher.group(i));
            }
            PotionType outputType = inputType;
            if (output.outputType.isPresent()) {
                outputType = output.outputType.get();
            }
            if (PotionConfigMod.hasCustomPotion(outputPattern, Optional.of(outputType))) {
                results.add(new Result(output.weight, outputPattern, outputType));
            }
        }
        return results;
    }
    public ItemStack craft(PotionType inputType, Potion potion) {
        List<Result> options = craftOptions(inputType, potion);
        int totalWeight = 0;
        for (Result option : options) {
            totalWeight += option.weight;
        }
        int random = (int) Math.floor(Math.random() * (double) totalWeight);
        for (Result option : options) {
            if (random < option.weight) {
                Potion outputPotion = PotionConfigMod.getOriginalPotion(option.customId, option.outputType);
                return switch (option.outputType) {
                    case Normal -> PotionUtil.setPotion(new ItemStack(Items.POTION), outputPotion);
                    case Splash -> PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), outputPotion);
                    case Lingering -> PotionUtil.setPotion(new ItemStack(Items.LINGERING_POTION), outputPotion);
                };
            }
            random -= option.weight;
        }
        PotionConfigMod.LOGGER.warn("No valid output found for potion-recipe with input '" + this.input + "' for " + inputType + " potion " + Registry.POTION.getId(potion));
        return new ItemStack(Items.POTION);
    }

    public static Pattern patternFromString(String wildcardedString, boolean isInput) {
        wildcardedString = wildcardedString.replace("\\", "\\\\")
                .replace("^", "\\^")
                .replace("$", "\\$")
                .replace(".", "\\.")
                .replace("|", "\\|")
                .replace("?", "\\?")
                .replace("+", "\\+")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("[", "\\[")
                .replace("]", "\\]");
        if (!isInput)
            wildcardedString = wildcardedString.replaceAll("\\{(\\d+)\\}", "($1)");
        wildcardedString = wildcardedString.replace("{", "\\{")
                .replace("}", "\\}");
        if (isInput)
            wildcardedString = wildcardedString.replace("*", "(.*)");
        return Pattern.compile(wildcardedString);
    }


    public record Output(int weight, String outputPattern, Optional<PotionType> outputType) {
    }
    public record Result(int weight, String customId, PotionType outputType) {
    }
}
