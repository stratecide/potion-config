package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.CustomRecipe;
import com.stratecide.potion_config.ModdedBrewingRecipeRegistry;
import com.stratecide.potion_config.PotionConfigMod;
import com.stratecide.potion_config.PotionType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingRecipeRegistry.class)
public abstract class BrewingRecipeRegistryMixin {

    @Inject(method = "isValidIngredient", at = @At("HEAD"), cancellable = true)
    private static void injectIsValidIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(PotionConfigMod.VALID_INGREDIENTS.contains(stack.getItem()));
    }

    @Inject(method = "hasRecipe", at = @At("HEAD"), cancellable = true)
    private static void injectHasRecipe(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (!(input.getItem() instanceof PotionItem))
            cir.setReturnValue(false);
        PotionItem item = (PotionItem) input.getItem();
        for (CustomRecipe recipe : PotionConfigMod.CUSTOM_RECIPES) {
            if (recipe.matches(PotionType.from(item), PotionUtil.getPotion(input), ingredient)) {
                cir.setReturnValue(true);
                return;
            }
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private static void injectCraft(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if (!input.isEmpty() && input.getItem() instanceof PotionItem) {
            PotionItem item = (PotionItem) input.getItem();
            for (CustomRecipe recipe : PotionConfigMod.CUSTOM_RECIPES) {
                if (recipe.matches(PotionType.from(item), PotionUtil.getPotion(input), ingredient)) {
                    cir.setReturnValue(recipe.craft(PotionType.from(item), PotionUtil.getPotion(input)));
                    return;
                }
            }
        } else {
            cir.setReturnValue(input);
            return;
        }
    }

    @Inject(method = "registerDefaults", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/BrewingRecipeRegistry;registerPotionRecipe(Lnet/minecraft/potion/Potion;Lnet/minecraft/item/Item;Lnet/minecraft/potion/Potion;)V"), cancellable = true)
    private static void blockDefaultRecipes(CallbackInfo ci) {
        ci.cancel();
    }

    /*@Inject(method = "isBrewable", at = @At("HEAD"), cancellable = true)
    private static void injectIsBrewable(Potion potion, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(PotionConfigMod.ARROW_POTIONS.contains(potion));
    }*/

}
