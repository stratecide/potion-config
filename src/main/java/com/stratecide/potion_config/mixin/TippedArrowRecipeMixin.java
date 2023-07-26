package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.ArrowRecipe;
import com.stratecide.potion_config.PotionConfigMod;
import com.stratecide.potion_config.PotionType;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.TippedArrowRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TippedArrowRecipe.class)
public abstract class TippedArrowRecipeMixin {

    @Inject(method = "matches(Lnet/minecraft/inventory/RecipeInputInventory;Lnet/minecraft/world/World;)Z", at = @At("HEAD"), cancellable = true)
    void injectMatches(RecipeInputInventory recipeInputInventory, World world, CallbackInfoReturnable<Boolean> cir) {
        if (recipeInputInventory.getWidth() != 3 || recipeInputInventory.getHeight() != 3) {
            cir.setReturnValue(false);
        }
        Potion potion = null;
        PotionType potionType = null;
        for(int i = 0; i < recipeInputInventory.getWidth(); ++i) {
            for(int j = 0; j < recipeInputInventory.getHeight(); ++j) {
                ItemStack itemStack = recipeInputInventory.getStack(i + j * recipeInputInventory.getWidth());
                if (itemStack.isEmpty()) {
                    cir.setReturnValue(false);
                    return;
                }

                if (i == 1 && j == 1) {
                    if (!(itemStack.getItem() instanceof PotionItem)) {
                        cir.setReturnValue(false);
                        return;
                    } else {
                        potion = PotionUtil.getPotion(itemStack);
                        potionType = PotionType.from((PotionItem) itemStack.getItem());
                    }
                } else if (!itemStack.isOf(Items.ARROW)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
        if (potion != null) {
            for (ArrowRecipe recipe : PotionConfigMod.ARROW_RECIPES) {
                if (recipe.matches(potionType, potion)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "craft(Lnet/minecraft/inventory/RecipeInputInventory;Lnet/minecraft/registry/DynamicRegistryManager;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    void injectCraft(RecipeInputInventory recipeInputInventory, DynamicRegistryManager dynamicRegistryManager, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack itemStack = recipeInputInventory.getStack(1 + recipeInputInventory.getWidth());
        Potion potion = PotionUtil.getPotion(itemStack);
        PotionType potionType = PotionType.from((PotionItem) itemStack.getItem());
        for (ArrowRecipe recipe : PotionConfigMod.ARROW_RECIPES) {
            if (recipe.matches(potionType, potion)) {
                cir.setReturnValue(recipe.craft(potionType, potion));
                return;
            }
        }
        cir.setReturnValue(ItemStack.EMPTY);
    }
}
