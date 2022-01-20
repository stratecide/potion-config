package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.ModdedBrewingRecipeRegistry;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingRecipeRegistry.class)
public abstract class BrewingRecipeRegistryMixin {

    @Inject(method = "isValidIngredient", at = @At("HEAD"), cancellable = true)
    private static void injectIsValidIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ModdedBrewingRecipeRegistry.isValidIngredient(stack));
    }

    @Inject(method = "hasRecipe", at = @At("HEAD"), cancellable = true)
    private static void injectHasRecipe(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ModdedBrewingRecipeRegistry.hasRecipe(input, ingredient));
    }

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private static void injectCraft(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(ModdedBrewingRecipeRegistry.craft(input, ingredient));
    }

    @Inject(method = "isBrewable", at = @At("HEAD"), cancellable = true)
    private static void injectIsBrewable(Potion potion, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(PotionConfigMod.ARROW_POTIONS.contains(potion));
    }

}
