package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.projectile.ArrowEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ArrowEntity.class)
public abstract class ArrowEntityMixin extends PersistentProjectileEntity {
    private ArrowEntityMixin(ArrowEntityMixin asdf) {
        super(null, null);
        throw new RuntimeException("ArrowEntityMixin's constructor should never be called");
    }
    @Shadow private Potion potion;

    @Redirect(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/Potion;getEffects()Ljava/util/List;"))
    private List<StatusEffectInstance> dontDivideBy8(Potion potion) {
        return new ArrayList<>();
    }

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z"))
    private void injectPotionEffects(LivingEntity target, CallbackInfo ci) {
        for (StatusEffectInstance statusEffectInstance : PotionConfigMod.getArrowPotion(potion).generateEffectInstances()) {
            target.addStatusEffect(statusEffectInstance, this.getEffectCause());
        }
    }
}
