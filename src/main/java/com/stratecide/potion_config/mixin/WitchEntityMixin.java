package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WitchEntity.class)
public abstract class WitchEntityMixin {

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtil;setPotion(Lnet/minecraft/item/ItemStack;Lnet/minecraft/potion/Potion;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack redirectPotion(ItemStack stack, Potion potion) {
        Identifier potionId = Registry.POTION.getId(potion);
        potion = PotionConfigMod.WITCH_POTIONS.get(potionId.getPath());
        return PotionUtil.setPotion(stack, potion);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtil;setPotion(Lnet/minecraft/item/ItemStack;Lnet/minecraft/potion/Potion;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack redirectPotion2(ItemStack stack, Potion potion) {
        Identifier potionId = Registry.POTION.getId(potion);
        potion = PotionConfigMod.WITCH_POTIONS.get("splash_" + potionId.getPath());
        return PotionUtil.setPotion(stack, potion);
    }
}
