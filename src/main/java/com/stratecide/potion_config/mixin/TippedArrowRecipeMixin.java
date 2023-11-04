package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.TippedArrowRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TippedArrowRecipe.class)
public abstract class TippedArrowRecipeMixin {

    @Redirect(method = "matches", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    boolean injectMatches(ItemStack itemStack, Item lingeringPotionItem) {
        if (lingeringPotionItem != Items.LINGERING_POTION)
            return itemStack.isOf(lingeringPotionItem);
        if (itemStack.isOf(PotionConfigMod.CRAFTING_POTION)) {
            Potion vanillaPotion = PotionUtil.getPotion(itemStack);
            CustomPotion potion = PotionConfigMod.getCustomPotion(vanillaPotion);
            return potion.canBeArrow();
        }
        return false;
    }

    @Redirect(method = "craft(Lnet/minecraft/inventory/RecipeInputInventory;Lnet/minecraft/registry/DynamicRegistryManager;)Lnet/minecraft/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    boolean injectCraft(ItemStack itemStack, Item lingeringPotionItem) {
        if (itemStack.isOf(PotionConfigMod.CRAFTING_POTION)) {
            Potion vanillaPotion = PotionUtil.getPotion(itemStack);
            CustomPotion potion = PotionConfigMod.getCustomPotion(vanillaPotion);
            return potion.canBeArrow();
        }
        return false;
    }
}
