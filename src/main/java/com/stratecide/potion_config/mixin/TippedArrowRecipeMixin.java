package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.ModdedBrewingRecipeRegistry;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.TippedArrowRecipe;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TippedArrowRecipe.class)
public abstract class TippedArrowRecipeMixin {

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    void injectMatches(CraftingInventory craftingInventory, World world, CallbackInfoReturnable<Boolean> cir) {
        if (craftingInventory.getWidth() == 3 && craftingInventory.getHeight() == 3) {
            Potion potion = null;
            for(int i = 0; i < craftingInventory.getWidth(); ++i) {
                for(int j = 0; j < craftingInventory.getHeight(); ++j) {
                    ItemStack itemStack = craftingInventory.getStack(i + j * craftingInventory.getWidth());
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
                        }
                    } else if (!itemStack.isOf(Items.ARROW)) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
            if (potion != null) {
                cir.setReturnValue(ModdedBrewingRecipeRegistry.canCraftArrows(potion));
                return;
            }
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    void injectCraft(CraftingInventory craftingInventory, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack itemStack = craftingInventory.getStack(1 + craftingInventory.getWidth());
        Potion potion = PotionUtil.getPotion(itemStack);
        if (potion == Potions.EMPTY) {
            cir.setReturnValue(ItemStack.EMPTY);
        } else {
            cir.setReturnValue(ModdedBrewingRecipeRegistry.craftArrows(potion, itemStack));
        }
    }
}
