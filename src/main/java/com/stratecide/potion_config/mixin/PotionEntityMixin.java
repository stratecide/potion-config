package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.effects.CustomStatusEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.projectile.thrown.PotionEntity;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PotionEntity.class)
public class PotionEntityMixin {
    @Redirect(method = "applySplashPotion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z"))
    private boolean hideParticlesAndAfterEffect(LivingEntity livingEntity, StatusEffectInstance statusEffectInstance, Entity source) {
        return livingEntity.addStatusEffect(new StatusEffectInstance(statusEffectInstance.getEffectType(), statusEffectInstance.getDuration(), statusEffectInstance.getAmplifier(), statusEffectInstance.isAmbient(), statusEffectInstance.shouldShowParticles(), statusEffectInstance.shouldShowParticles() && CustomStatusEffect.showIcon(statusEffectInstance.getEffectType())), source);
    }
}
