package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WitchEntity.class)
public abstract class WitchEntityMixin {

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtil;setPotion(Lnet/minecraft/item/ItemStack;Lnet/minecraft/potion/Potion;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack redirectPotion(ItemStack stack, Potion potion) {
        String potionId = "water_breathing";
        if (potion == Potions.FIRE_RESISTANCE)
            potionId = "fire_resistance";
        if (potion == Potions.HEALING)
            potionId = "healing";
        if (potion == Potions.SWIFTNESS)
            potionId = "swiftness";
        return PotionUtil.setPotion(stack, PotionConfigMod.WITCH_POTIONS.get("normal-" + potionId));
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtil;setPotion(Lnet/minecraft/item/ItemStack;Lnet/minecraft/potion/Potion;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack redirectPotion2(ItemStack stack, Potion potion) {
        String potionId = "harming";
        if (potion == Potions.HEALING)
            potionId = "healing";
        if (potion == Potions.REGENERATION)
            potionId = "regeneration";
        if (potion == Potions.SLOWNESS)
            potionId = "slowness";
        if (potion == Potions.POISON)
            potionId = "poison";
        if (potion == Potions.WEAKNESS)
            potionId = "weakness";
        Potion replacement = PotionConfigMod.WITCH_POTIONS.get(PotionConfigMod.PREFIX_SPLASH + potionId);
        if (PotionConfigMod.LINGERING_POTIONS.contains(replacement))
            stack = new ItemStack(Items.SPLASH_POTION);
        return PotionUtil.setPotion(stack, replacement);
    }
}
