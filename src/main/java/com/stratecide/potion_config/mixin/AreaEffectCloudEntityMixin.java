package com.stratecide.potion_config.mixin;

import com.google.common.collect.Lists;
import com.stratecide.potion_config.CustomPotion;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(AreaEffectCloudEntity.class)
public abstract class AreaEffectCloudEntityMixin extends Entity {

    public AreaEffectCloudEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract Potion getPotion();

    @Shadow @Final private List<StatusEffectInstance> effects;

    @Shadow public abstract @Nullable LivingEntity getOwner();

    @Shadow public abstract int getColor();

    @Shadow public abstract void setColor(int rgb);

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateColor(CallbackInfo ci) {
        if (this.getWorld().isClient) {
            return;
        }
        CustomPotion potion = PotionConfigMod.getCustomPotion(this.getPotion());
        int color = potion.getColor(false);
        if (color != getColor()) {
            setColor(color);
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 0))
    private boolean assumeEffectsNotEmpty(List list) {
        while (list.size() > 0) {
            list.remove(0);
        }
        return false;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object generateEffectList(Map<Entity, Integer> map, Object entityObject, Object ageObject) {
        LivingEntity entity = (LivingEntity) entityObject;
        Integer result = map.put(entity, (Integer) ageObject);
        // the list is re-generated for each LivingEntity because effects can have a less than 100% chance to affect an entity
        CustomPotion potion = PotionConfigMod.getCustomPotion(this.getPotion());
        List<StatusEffectInstance> effects = potion.generateEffectInstances();
        effects.addAll(this.effects);
        AreaEffectCloudEntity self = (AreaEffectCloudEntity) ((Object) this);
        for (StatusEffectInstance statusEffectInstance : effects) {
            if (statusEffectInstance.getEffectType().isInstant()) {
                statusEffectInstance.getEffectType().applyInstantEffect(self, this.getOwner(), entity, statusEffectInstance.getAmplifier(), 0.5);
            } else {
                entity.addStatusEffect(statusEffectInstance, self);
            }
        }
        return result;
    }
}
