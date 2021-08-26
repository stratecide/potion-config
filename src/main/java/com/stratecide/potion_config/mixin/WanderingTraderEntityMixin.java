package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin {

    @Redirect(method = "initGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtil;setPotion(Lnet/minecraft/item/ItemStack;Lnet/minecraft/potion/Potion;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack redirectPotion2(ItemStack stack, Potion potion) {
        return PotionUtil.setPotion(stack, PotionConfigMod.WANDERING_TRADER_POTION);
    }
}
