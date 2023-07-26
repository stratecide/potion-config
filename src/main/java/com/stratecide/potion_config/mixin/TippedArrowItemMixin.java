package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import com.stratecide.potion_config.PotionType;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

@Mixin(TippedArrowItem.class)
public abstract class TippedArrowItemMixin extends Item {

    public TippedArrowItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "getTranslationKey", at = @At("HEAD"), cancellable = true)
    void properTranslationKey(ItemStack stack, CallbackInfoReturnable<String> cir) {
        Potion potion = PotionUtil.getPotion(stack);
        String postfix = PotionConfigMod.getCustomArrowPotionId(potion);
        cir.setReturnValue(this.getTranslationKey() + ".effect." + (postfix != null ? postfix : "mystery"));
    }

    @Inject(method = "appendStacks", at = @At("HEAD"), cancellable = true)
    void removeFromCreativeInventory(ItemGroup group, DefaultedList<ItemStack> stacks, CallbackInfo ci) {
        if (this.isIn(group)) {
            for (Potion potion : Registry.POTION) {
                if (potion != Potions.EMPTY && PotionConfigMod.hasArrowPotion(potion)) {
                    stacks.add(PotionUtil.setPotion(new ItemStack(this), potion));
                }
            }
            ci.cancel();
        }
    }
}
