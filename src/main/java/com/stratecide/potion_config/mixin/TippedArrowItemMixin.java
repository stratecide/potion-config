package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
}
