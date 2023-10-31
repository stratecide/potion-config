package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.effects.AfterEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectInstance.class)
public abstract class StatusEffectInstanceMixin {
    @Shadow public abstract StatusEffect getEffectType();

    @Inject(method = "update", at = @At("TAIL"))
    private void applyAfterEffects(LivingEntity entity, Runnable overwriteCallback, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && this.getEffectType() instanceof AfterEffect) {
            AfterEffect afterEffect = (AfterEffect) this.getEffectType();
            for (StatusEffectInstance effect : afterEffect.generateEffectInstances()) {
                entity.addStatusEffect(effect);
            }
        }
    }
}
