package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.effects.CustomStatusEffect;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.stream.Collectors;

@Mixin(AbstractInventoryScreen.class)
public class AbstractInventoryScreenMixin {
    @Redirect(method = "drawStatusEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStatusEffects()Ljava/util/Collection;"))
    public Collection<StatusEffectInstance> hideAfterEffects(ClientPlayerEntity player) {
        Collection<StatusEffectInstance> collection = player.getStatusEffects();
        return collection.stream()
                .filter(statusEffectInstance -> CustomStatusEffect.showIcon(statusEffectInstance.getEffectType()))
                .collect(Collectors.toUnmodifiableSet());
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
