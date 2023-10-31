package com.stratecide.potion_config.mixin;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractInventoryScreen.class)
public class AbstractInventoryScreenMixin {
    @Inject(method = "getStatusEffectDescription", at = @At("HEAD"))
    private void replaceStatusEffectPotency(StatusEffectInstance statusEffect, CallbackInfoReturnable<Text> cir) {
        if (statusEffect.getAmplifier() > 10) {
            MutableText mutableText = statusEffect.getEffectType().getName().copy();
            mutableText.append(" ").append("" + (statusEffect.getAmplifier() + 1));
            cir.setReturnValue(mutableText);
        }
    }
}
