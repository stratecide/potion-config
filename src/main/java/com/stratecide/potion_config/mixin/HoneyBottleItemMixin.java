package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.CustomPotion;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.HoneyBottleItem;
import net.minecraft.potion.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HoneyBottleItem.class)
public class HoneyBottleItemMixin {
    @Redirect(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;removeStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
    boolean milkBucketPotion(LivingEntity user, StatusEffect type) {
        if (PotionConfigMod.HONEY_BOTTLE_POTION != Potions.EMPTY) {
            CustomPotion potion = PotionConfigMod.getCustomPotion(PotionConfigMod.HONEY_BOTTLE_POTION);
            for (StatusEffectInstance statusEffectInstance : potion.generateEffectInstances()) {
                if (statusEffectInstance.getEffectType().isInstant()) {
                    statusEffectInstance.getEffectType().applyInstantEffect(user, null, user, statusEffectInstance.getAmplifier(), 1.0);
                } else {
                    user.addStatusEffect(statusEffectInstance);
                }
            }
            return true;
        } else {
            return user.removeStatusEffect(StatusEffects.POISON);
        }
    }
}
