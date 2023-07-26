package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.CustomPotion;
import com.stratecide.potion_config.PotionConfigMod;
import com.stratecide.potion_config.effects.CustomStatusEffect;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.projectile.thrown.PotionEntity;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin extends ThrownItemEntity {
    public PotionEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    // sometimes when a lingering potion hits the top of a mob, the cloud is too high to affect the mob
    @Inject(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/thrown/PotionEntity;applyLingeringPotion(Lnet/minecraft/item/ItemStack;Lnet/minecraft/potion/Potion;)V"))
    private void fixTopCollision(HitResult hitResult, CallbackInfo ci) {
        if (hitResult.getType() == HitResult.Type.ENTITY && ((EntityHitResult)hitResult).getEntity() instanceof LivingEntity) {
            Entity entity = ((EntityHitResult)hitResult).getEntity();
            if (getY() >= entity.getBoundingBox().maxY) {
                setPos(getX(), entity.getBoundingBox().maxY - 0.1, getZ());
            }
        }
    }

    @Redirect(method = "applySplashPotion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z"))
    private boolean hideParticlesAndAfterEffect(LivingEntity livingEntity, StatusEffectInstance statusEffectInstance, Entity source) {
        return livingEntity.addStatusEffect(new StatusEffectInstance(statusEffectInstance.getEffectType(), statusEffectInstance.getDuration(), statusEffectInstance.getAmplifier(), statusEffectInstance.isAmbient(), statusEffectInstance.shouldShowParticles(), statusEffectInstance.shouldShowParticles() && CustomStatusEffect.showIcon(statusEffectInstance.getEffectType())), source);
    }
}
