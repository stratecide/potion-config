package com.stratecide.potion_config.blocks.floor;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class FloorBlockRecipe extends SpecialCraftingRecipe {

    public FloorBlockRecipe(Identifier identifier) {
        super(identifier);
    }

    private FloorBlockRecipeContainer getMeta(Potion potion) {
        return PotionConfigMod.FLOOR_BLOCK_RECIPES.get(potion);
    }

    @Override
    public boolean matches(CraftingInventory craftingInventory, World world) {
        if (craftingInventory.getWidth() != 3 || craftingInventory.getHeight() != 3) {
            return false;
        }
        Potion potion = PotionUtil.getPotion(craftingInventory.getStack(4));
        if (!PotionConfigMod.FLOOR_BLOCK_RECIPES.containsKey(potion))
            return false;
        for (int i = 0; i < craftingInventory.getWidth(); ++i) {
            for (int j = 0; j < craftingInventory.getHeight(); ++j) {
                ItemStack itemStack = craftingInventory.getStack(i + j * craftingInventory.getWidth());
                if (itemStack.isEmpty()) {
                    return false;
                }
                if (i == 1 && j == 1) {
                    if (!itemStack.isOf(PotionConfigMod.CRAFTING_POTION))
                        return false;
                } else if (!getMeta(potion).ingredient().test(itemStack)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory) {
        ItemStack itemStack = craftingInventory.getStack(4);
        Potion potion = PotionUtil.getPotion(itemStack);
        if (!itemStack.isOf(PotionConfigMod.CRAFTING_POTION) || !PotionConfigMod.FLOOR_BLOCK_RECIPES.containsKey(potion)) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack2 = new ItemStack(PotionConfigMod.FLOOR_BLOCKS.get(potion), getMeta(potion).outputCount());
        PotionUtil.setCustomPotionEffects(itemStack2, PotionUtil.getCustomPotionEffects(itemStack));
        return itemStack2;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PotionConfigMod.FLOOR_BLOCK_RECIPE;
    }
}

