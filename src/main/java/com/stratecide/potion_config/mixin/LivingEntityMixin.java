package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionColorList;
import com.stratecide.potion_config.PotionConfigMod;
import com.stratecide.potion_config.blocks.floor.FloorBlock;
import com.stratecide.potion_config.effects.AfterEffect;
import com.stratecide.potion_config.effects.CustomStatusEffect;
import com.stratecide.potion_config.effects.Particles;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
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

    @Shadow public abstract boolean addStatusEffect(StatusEffectInstance effect);

    @Shadow public abstract int getRoll();

    @Shadow public abstract boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource);

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
            Particles statusEffect = (Particles) Registry.STATUS_EFFECT.get(Particles.generateIdentifier(entry.getKey()));
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

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInsideWall()Z"))
    private void handlePotionBlocks(CallbackInfo ci) {
        if (!this.isSpectator()) {
            BlockState state = this.world.getBlockState(this.getVelocityAffectingPos());
            if (state.getBlock() instanceof FloorBlock) {
                FloorBlock block = (FloorBlock) state.getBlock();
                for (StatusEffectInstance statusEffectInstance : block.getPotion().generateEffectInstances()) {
                    addStatusEffect(statusEffectInstance);
                }
            }
        }
    }

    @Inject(method = "tickFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"), cancellable = true)
    private void injectElytraEffect(CallbackInfo ci) {
        if (hasStatusEffect(CustomStatusEffect.ELYTRA)) {
            if (!this.world.isClient && (getRoll() + 1) % 10 == 0) {
                this.emitGameEvent(GameEvent.ELYTRA_GLIDE);
            }
            ci.cancel();
        }
    }

    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void injectNoFallDamageEffect(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (fallDistance > 0.f && damageSource == DamageSource.FALL && hasStatusEffect(CustomStatusEffect.NO_FALL_DAMAGE)) {
            cir.setReturnValue(handleFallDamage(0.f, damageMultiplier, damageSource));
        }
    }

    @Inject(method = "getStatusEffects", at = @At("HEAD"), cancellable = true)
    private void hideAfterEffects(CallbackInfoReturnable<Collection<StatusEffectInstance>> cir) {
        // hacky, but redirecting in AbstractInventoryScreen causes collision with REI
        if (PotionConfigMod.HIDE_AFTER_EFFECTS_DISPLAY) {
            PotionConfigMod.HIDE_AFTER_EFFECTS_DISPLAY = false;
            cir.setReturnValue(getActiveStatusEffects().values()
                .stream()
                .filter(effectInstance -> {
                    StatusEffect effect = effectInstance.getEffectType();
                    return !(effect instanceof AfterEffect) && !(effect instanceof Particles);
                }).collect(Collectors.toList()));
        }
    }

    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.01))
    private double improveSlowFalling(double d) {
        if (this.isSneaking())
            return 0.04;
        else
            return d;
    }
}
