package com.stratecide.potion_config.mixin;

import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingRecipeRegistry.class)
public abstract class BrewingRecipeRegistryMixin {

    @Inject(method = "registerPotionRecipe", at = @At("HEAD"), cancellable = true)
    private static void blockDefaultRecipes(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "registerItemRecipe", at = @At("HEAD"), cancellable = true)
    private static void blockDefaultRecipes2(CallbackInfo ci) {
        ci.cancel();
    }
}
