package com.stratecide.potion_config.mixin;

import com.google.common.collect.Maps;
import com.stratecide.potion_config.PotionColorList;
import com.stratecide.potion_config.PotionConfigMod;
import com.stratecide.potion_config.effects.CustomStatusEffect;
import com.stratecide.potion_config.effects.Particles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Collectors;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    private static final TrackedData<PotionColorList> POTION_PARTICLES = DataTracker.registerData(LivingEntity.class, PotionConfigMod.POTION_PARTICLE_COLORS);

    public LivingEntityMixin(LivingEntityMixin asdf) {
        super(null, null);
    }

    @Shadow public abstract AttributeContainer getAttributes();

    @Shadow public abstract Map<StatusEffect, StatusEffectInstance> getActiveStatusEffects();

    @Shadow public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow public abstract @Nullable StatusEffectInstance getStatusEffect(StatusEffect effect);

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void injectPotionColorTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(POTION_PARTICLES, new PotionColorList(new HashMap<>()));
    }

    @Inject(method = "updatePotionVisibility", at = @At("TAIL"))
    private void injectPotionColorUpdate(CallbackInfo ci) {
        Map<Integer, Integer> map = new HashMap<>();
        for (StatusEffectInstance statusEffectInstance : this.getActiveStatusEffects().values().stream()
                .filter(statusEffectInstance -> statusEffectInstance.getEffectType() instanceof Particles)
                .collect(Collectors.toList())) {
            map.put(((Particles) statusEffectInstance.getEffectType()).id, statusEffectInstance.getAmplifier());
        }
        this.dataTracker.set(POTION_PARTICLES, new PotionColorList(map));
    }

    @Inject(method = "tickStatusEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;get(Lnet/minecraft/entity/data/TrackedData;)Ljava/lang/Object;"), cancellable = true)
    private void replacePotionParticles(CallbackInfo ci) {
        PotionColorList colorList = this.dataTracker.get(POTION_PARTICLES);
        for (Map.Entry<Integer, Integer> entry : colorList.collection().entrySet()) {
            Particles statusEffect = (Particles) Registries.STATUS_EFFECT.get(Particles.generateIdentifier(entry.getKey()));
            statusEffect.tick((LivingEntity) ((Object) this), entry.getValue());
        }
        ci.cancel();
    }

    @Inject(method = "onStatusEffectUpgraded", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/attribute/AttributeContainer;I)V"), cancellable = true)
    private void reapplyStatusEffect(StatusEffectInstance effect, boolean reapplyEffect, Entity source, CallbackInfo ci) {
        StatusEffect statusEffect = effect.getEffectType();
        if (statusEffect instanceof CustomStatusEffect) {
            LivingEntity self = (LivingEntity) ((Object) this);
            CustomStatusEffect custom = (CustomStatusEffect) statusEffect;
            custom.onReapplied(self, this.getAttributes(), effect.getAmplifier());
            ci.cancel();
        }
    }

    @Inject(method = "tickStatusEffects", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"))
    private void fixEffectRemoval(CallbackInfo ci) {
        if (!getWorld().isClient) {
            LivingEntity self = (LivingEntity) (Object) this;
            for (StatusEffectInstance statusEffectInstance : this.getActiveStatusEffects().values()) {
                if (statusEffectInstance.getDuration() <= 0 && statusEffectInstance.getEffectType() instanceof CustomStatusEffect) {
                    ((CustomStatusEffect) statusEffectInstance.getEffectType()).onRemoved1(self, this.getAttributes(), statusEffectInstance.getAmplifier());
                }
            }
        }
    }

    @Inject(method = "clearStatusEffects", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"))
    private void fixClearStatusEffects(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        for (StatusEffectInstance statusEffectInstance : this.getActiveStatusEffects().values()) {
            if (statusEffectInstance.getEffectType() instanceof CustomStatusEffect) {
                ((CustomStatusEffect) statusEffectInstance.getEffectType()).onRemoved1(self, this.getAttributes(), statusEffectInstance.getAmplifier());
            }
        }
    }

    @Inject(method = "getJumpBoostVelocityModifier", at = @At("TAIL"), cancellable = true)
    private void reduceJumpHeight(CallbackInfoReturnable<Double> cir) {
        if (hasStatusEffect(CustomStatusEffect.JUMP_DROP)) {
            double reduction = 0.1 * (double)(getStatusEffect(CustomStatusEffect.JUMP_DROP).getAmplifier() + 1);
            cir.setReturnValue(cir.getReturnValueD() - reduction);
        }
    }
}
