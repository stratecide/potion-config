package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.CustomPotion;
import com.stratecide.potion_config.PotionConfigMod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow public abstract int getColor();

    @Shadow @Final private static TrackedData<Integer> COLOR;

    @Shadow private boolean colorSet;

    @Inject(method = "initColor", at = @At("HEAD"), cancellable = true)
    private void initColor(CallbackInfo ci) {
        this.colorSet = false;
        if (this.potion == Potions.EMPTY) {
            this.dataTracker.set(COLOR, -1);
        } else {
            CustomPotion potion = PotionConfigMod.getCustomPotion(this.potion);
            int color = potion.getColor(false);
            this.dataTracker.set(COLOR, color);
        }
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateColor(CallbackInfo ci) {
        if (this.world.isClient || colorSet || this.potion == Potions.EMPTY) {
            return;
        }
        CustomPotion potion = PotionConfigMod.getCustomPotion(this.potion);
        int color = potion.getColor(false);
        if (color != getColor()) {
            this.dataTracker.set(COLOR, color);
        }
    }

    @Redirect(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/Potion;getEffects()Ljava/util/List;"))
    private List<StatusEffectInstance> dontDivideBy8(Potion potion) {
        return new ArrayList<>();
    }

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z"))
    private void injectPotionEffects(LivingEntity target, CallbackInfo ci) {
        if (this.potion != Potions.EMPTY) {
            for (StatusEffectInstance statusEffectInstance : PotionConfigMod.getCustomPotion(this.potion).generateEffectInstances()) {
                if (statusEffectInstance.getEffectType().isInstant()) {
                    statusEffectInstance.getEffectType().applyInstantEffect(getEventSource(), getEffectCause(), target, statusEffectInstance.getAmplifier(), 1.0);
                } else {
                    target.addStatusEffect(statusEffectInstance, this.getEffectCause());
                }
            }
        }
    }
}
