package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import com.stratecide.potion_config.effects.AfterEffect;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.stream.Collectors;

@Mixin(AbstractInventoryScreen.class)
public class AbstractInventoryScreenMixin {
    @Inject(method = "drawStatusEffects", at = @At("HEAD"))
    private void hideAfterEffects(MatrixStack matrices, int mouseX, int mouseY, CallbackInfo ci) {
        PotionConfigMod.HIDE_AFTER_EFFECTS_DISPLAY = true;
    }

    @Inject(method = "getStatusEffectDescription", at = @At("HEAD"))
    private void replaceStatusEffectPotency(StatusEffectInstance statusEffect, CallbackInfoReturnable<Text> cir) {
        if (statusEffect.getAmplifier() > 10) {
            MutableText mutableText = statusEffect.getEffectType().getName().copy();
            mutableText.append(" ").append("" + (statusEffect.getAmplifier() + 1));
            cir.setReturnValue(mutableText);
        }
    }
}
