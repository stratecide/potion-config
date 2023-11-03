package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.CustomPotion;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MilkBucketItem.class)
public class MilkBucketItemMixin {

    @Redirect(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z"))
    boolean milkBucketPotion(LivingEntity entity) {
        Potion milkPotion = Registry.POTION.get(PotionConfigMod.MILK_BUCKET_POTION);
        if (milkPotion != Potions.EMPTY) {
            CustomPotion potion = PotionConfigMod.getCustomPotion(milkPotion);
            for (StatusEffectInstance statusEffectInstance : potion.generateEffectInstances()) {
                entity.addStatusEffect(statusEffectInstance);
            }
            return true;
        } else {
            return entity.clearStatusEffects();
        }
    }
}