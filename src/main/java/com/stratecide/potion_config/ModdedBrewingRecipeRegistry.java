package com.stratecide.potion_config;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModdedBrewingRecipeRegistry {

    private static final List<Recipe> RECIPES = Lists.newArrayList();
    private static final List<ArrowRecipe> ARROW_RECIPES = Lists.newArrayList();

    private static boolean parsedRecipes = false;

    private static void ensureRecipesParsed() {
        if (parsedRecipes)
            return;
        parsedRecipes = true;

        JsonObject ingredientGroups = null;
        if (PotionConfigMod.config.has("ingredient_groups"))
            ingredientGroups = PotionConfigMod.config.get("ingredient_groups").getAsJsonObject();
        registerRecipes(ingredientGroups, PotionConfigMod.config.getAsJsonArray("recipes"));
        registerArrowRecipes(PotionConfigMod.config.get("arrow_recipes").getAsJsonArray());
    }

    public static boolean isValidIngredient(ItemStack ingredient) {
        ensureRecipesParsed();
        for (Recipe recipe : RECIPES) {
            if (recipe.ingredient.test(ingredient)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasRecipe(ItemStack inputPotion, ItemStack ingredient) {
        return findRecipe(inputPotion, ingredient) != null;
    }

    private static Recipe findRecipe(ItemStack inputPotion, ItemStack ingredient) {
        ensureRecipesParsed();
        if (inputPotion == null || inputPotion.isEmpty() || !(inputPotion.getItem() instanceof PotionItem) || ingredient == null || ingredient.isEmpty())
            return null;
        String potionId = Registry.POTION.getId(PotionUtil.getPotion(inputPotion)).getPath();
        for (Recipe recipe : RECIPES) {
            if (recipe.ingredient.test(ingredient) && potionId.matches(recipe.inputPotionPattern)) {
                Potion potion = generateOutputPotion(recipe.inputPotionPattern, recipe.outputPotionPattern, potionId);
                if (PotionConfigMod.NORMAL_POTIONS.contains(potion) || PotionConfigMod.SPLASH_POTIONS.contains(potion) || PotionConfigMod.LINGERING_POTIONS.contains(potion))
                    return recipe;
            }
        }
        return null;
    }

    public static void arrowIngredientTooltip(Potion outputPotion, List<Text> list) {
        ensureRecipesParsed();
        if (outputPotion == null || outputPotion == Potions.EMPTY)
            return;
        List<Text> options = new ArrayList<>();
        for (ArrowRecipe recipe : ARROW_RECIPES) {
            for (Identifier inputId : Registry.POTION.getIds()) {
                String potionId = inputId.getPath();
                if (potionId.matches(recipe.inputPotionPattern)) {
                    Potion potion = generateOutputPotion(recipe.inputPotionPattern, recipe.outputPotionPattern, potionId);
                    if (potion == outputPotion) {
                        Potion inputPotion = Registry.POTION.get(inputId);
                        Item pot = Items.POTION;
                        if (PotionConfigMod.SPLASH_POTIONS.contains(inputPotion))
                            pot = Items.SPLASH_POTION;
                        else if (PotionConfigMod.LINGERING_POTIONS.contains(inputPotion))
                            pot = Items.LINGERING_POTION;
                        ItemStack potionStack = PotionUtil.setPotion(pot.getDefaultStack(), inputPotion);
                        TranslatableText mutableText = new TranslatableText(Items.ARROW.getDefaultStack().getTranslationKey());
                        mutableText = new TranslatableText("potion-config.ingredients", new TranslatableText(potionStack.getTranslationKey()), mutableText);
                        options.add(mutableText.formatted(Formatting.DARK_GRAY));
                    }
                }
            }
        }
        if (options.size() > 0)
            list.add(options.get((int) ((new Date().getTime() / PotionConfigMod.TOOLTIP_MILLISECONDS) % options.size())));
    }

    public static void ingredientTooltip(Potion outputPotion, List<Text> list) {
        ensureRecipesParsed();
        if (outputPotion == null || outputPotion == Potions.EMPTY)
            return;
        List<Text> options = new ArrayList<>();
        for (Recipe recipe : RECIPES) {
            for (Identifier inputId : Registry.POTION.getIds()) {
                String potionId = inputId.getPath();
                if (potionId.matches(recipe.inputPotionPattern)) {
                    Potion potion = generateOutputPotion(recipe.inputPotionPattern, recipe.outputPotionPattern, potionId);
                    if (potion == outputPotion) {
                        Potion inputPotion = Registry.POTION.get(inputId);
                        Item pot = Items.POTION;
                        if (PotionConfigMod.SPLASH_POTIONS.contains(inputPotion))
                            pot = Items.SPLASH_POTION;
                        else if (PotionConfigMod.LINGERING_POTIONS.contains(inputPotion))
                            pot = Items.LINGERING_POTION;
                        ItemStack potionStack = PotionUtil.setPotion(pot.getDefaultStack(), inputPotion);
                        ItemStack[] ingredients = recipe.ingredient.getMatchingStacks();
                        for (ItemStack ingredient : ingredients) {
                            TranslatableText mutableText = new TranslatableText(ingredient.getTranslationKey());
                            mutableText = new TranslatableText("potion-config.ingredients", new TranslatableText(potionStack.getTranslationKey()), mutableText);
                            options.add(mutableText.formatted(Formatting.DARK_GRAY));
                        }
                    }
                }
            }
        }
        if (options.size() > 0)
            list.add(options.get((int) ((new Date().getTime() / PotionConfigMod.TOOLTIP_MILLISECONDS) % options.size())));
    }

    private static Potion generateOutputPotion(String inputPattern, String outputPattern, String inputPotionId) {
        Matcher matcher = Pattern.compile(inputPattern).matcher(inputPotionId);
        if (!matcher.matches())
            return Potions.EMPTY;
        for (int i = 1; i <= matcher.groupCount(); i++)
            outputPattern = outputPattern.replace("(" + i + ")", matcher.group(i));
        Potion potion = Registry.POTION.get(new Identifier(PotionConfigMod.MOD_ID + ":" + outputPattern));
        return potion;
    }

    public static ItemStack craft(ItemStack ingredient, ItemStack inputPotion) {
        Recipe recipe = findRecipe(inputPotion, ingredient);
        if (recipe != null) {
            Potion potion = generateOutputPotion(recipe.inputPotionPattern, recipe.outputPotionPattern, Registry.POTION.getId(PotionUtil.getPotion(inputPotion)).getPath());
            Item item = Items.POTION;
            if (PotionConfigMod.SPLASH_POTIONS.contains(potion))
                item = Items.SPLASH_POTION;
            else if (PotionConfigMod.LINGERING_POTIONS.contains(potion))
                item = Items.LINGERING_POTION;
            return PotionUtil.setPotion(new ItemStack(item), potion);
        }
        return inputPotion;
    }

    private static void registerRecipes(JsonObject itemGroups, JsonArray config) {
        // parse item groups
        Map<String, Ingredient> ingredients = new HashMap<>();
        if (itemGroups != null) {
            for (Map.Entry<String, JsonElement> entry : itemGroups.entrySet()) {
                List<Item> items = new ArrayList<>();
                for (Iterator<JsonElement> it = entry.getValue().getAsJsonArray().iterator(); it.hasNext(); ) {
                    String itemId = it.next().getAsString();
                    Item item = Registry.ITEM.get(new Identifier(itemId));
                    if (item == Items.AIR)
                        throw new AssertionError("unknown item '" + itemId + "' for ingredient group '" + entry.getKey() + "'");
                    items.add(item);
                }
                if (items.size() > 0) {
                    Ingredient ingredient = Ingredient.ofStacks(items.stream().map(ItemStack::new));
                    ingredients.put(entry.getKey(), ingredient);
                }
            }
        }

        // parse recipes
        for (Iterator<JsonElement> it = config.iterator(); it.hasNext(); ) {
            JsonObject element = (JsonObject) it.next();
            String ingredientId = element.get("ingredient").getAsString();
            Ingredient ingredient = ingredients.get(ingredientId);
            if (ingredient == null) {
                Item item = Registry.ITEM.get(new Identifier(ingredientId));
                if (item == Items.AIR)
                    throw new AssertionError("Invalid ingredient identifier : " + ingredientId);
                ingredient = Ingredient.ofItems(item);
            }
            String inputPattern = patternFromString(element.get("input").getAsString(), true).pattern();
            String outputPattern = patternFromString(element.get("output").getAsString(), false).pattern();
            boolean inputPotionFound = false;
            boolean outputPotionFound = false;
            for (Identifier id : Registry.POTION.getIds()) {
                if (PotionConfigMod.ARROW_POTIONS.contains(Registry.POTION.get(id)))
                    continue;
                if (id.getPath().matches(inputPattern)) {
                    inputPotionFound = true;
                    Potion output = generateOutputPotion(inputPattern, outputPattern, id.getPath());
                    if (PotionConfigMod.NORMAL_POTIONS.contains(output) || PotionConfigMod.SPLASH_POTIONS.contains(output) || PotionConfigMod.LINGERING_POTIONS.contains(output))
                        outputPotionFound = true;
                }
            }
            if (!inputPotionFound)
                throw new AssertionError("no potion matches the recipe input '" + element.get("input").getAsString() + "'");
            if (!outputPotionFound)
                throw new AssertionError("no potion matches the recipe output '" + element.get("output").getAsString() + "'");
            RECIPES.add(new Recipe(inputPattern, ingredient, outputPattern));
        }
    }

    private static Pattern patternFromString(String wildcardedString, boolean isInput) {
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

    private static void registerArrowRecipes(JsonArray config) {
        for (Iterator<JsonElement> it = config.iterator(); it.hasNext(); ) {
            JsonObject element = (JsonObject) it.next();
            String inputPattern = patternFromString(element.get("input").getAsString(), true).pattern();
            if (inputPattern.startsWith("arrow-"))
                throw new AssertionError("Input-Potions for arrow_recipes can't start with 'arrow-'");
            String outputPattern = "arrow-" + patternFromString(element.get("output").getAsString(), false).pattern();
            boolean inputPotionFound = false;
            boolean outputPotionFound = false;
            for (Identifier id : Registry.POTION.getIds()) {
                if (PotionConfigMod.ARROW_POTIONS.contains(Registry.POTION.get(id)))
                    continue;
                if (id.getPath().matches(inputPattern)) {
                    inputPotionFound = true;
                    Potion output = generateOutputPotion(inputPattern, outputPattern, id.getPath());
                    if (PotionConfigMod.ARROW_POTIONS.contains(output))
                        outputPotionFound = true;
                }
            }
            if (!inputPotionFound)
                throw new AssertionError("no potion matches the recipe input '" + element.get("input").getAsString() + "'");
            if (!outputPotionFound)
                throw new AssertionError("no potion matches the recipe output '" + element.get("output").getAsString() + "'");
            ARROW_RECIPES.add(new ArrowRecipe(inputPattern, outputPattern));
        }
    }

    private static ArrowRecipe findArrowRecipe(Potion inputPotion) {
        ensureRecipesParsed();
        String potionId = Registry.POTION.getId(inputPotion).getPath();
        for (ArrowRecipe recipe : ARROW_RECIPES) {
            if (potionId.matches(recipe.inputPotionPattern)) {
                Potion potion = generateOutputPotion(recipe.inputPotionPattern, recipe.outputPotionPattern, potionId);
                if (PotionConfigMod.ARROW_POTIONS.contains(potion))
                    return recipe;
            }
        }
        return null;
    }

    public static boolean canCraftArrows(Potion inputPotion) {
        return findArrowRecipe(inputPotion) != null;
    }

    public static ItemStack craftArrows(Potion inputPotion, ItemStack itemStack) {
        ArrowRecipe recipe = findArrowRecipe(inputPotion);
        ItemStack result = new ItemStack(Items.TIPPED_ARROW, 8);
        Potion potion = generateOutputPotion(recipe.inputPotionPattern, recipe.outputPotionPattern, Registry.POTION.getId(inputPotion).getPath());
        PotionUtil.setPotion(result, potion);
        PotionUtil.setCustomPotionEffects(result, PotionUtil.getCustomPotionEffects(itemStack));
        return result;
    }

    record Recipe(String inputPotionPattern, Ingredient ingredient, String outputPotionPattern) {
    }

    record ArrowRecipe(String inputPotionPattern, String outputPotionPattern) {
    }
}
