package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.screen.BrewingStandScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandScreenHandler.PotionSlot.class)
public class BrewingStandScreenHandlerPotionSlotMixin {

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private static void allowMorePotionTypes(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        boolean allow = stack.isOf(Items.POTION)
                || stack.isOf(Items.SPLASH_POTION)
                || stack.isOf(Items.LINGERING_POTION)
                || stack.isOf(PotionConfigMod.CRAFTING_POTION)
                || PotionConfigMod.HONEY_BOTTLE_POTION != Potions.EMPTY && stack.isOf(Items.HONEY_BOTTLE);
        cir.setReturnValue(allow);
    }
}
