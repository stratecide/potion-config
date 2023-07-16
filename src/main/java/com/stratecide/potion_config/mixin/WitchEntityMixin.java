package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
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
        if (PotionConfigMod.WITCH_POTIONS_NORMAL.containsKey(potionId)) {
            potion = PotionConfigMod.WITCH_POTIONS_NORMAL.get(potionId);
        }
        return PotionUtil.setPotion(stack, potion);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtil;setPotion(Lnet/minecraft/item/ItemStack;Lnet/minecraft/potion/Potion;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack redirectPotion2(ItemStack stack, Potion potion) {
        Identifier potionId = Registry.POTION.getId(potion);
        if (PotionConfigMod.WITCH_POTIONS_SPLASH.containsKey(potionId)) {
            potion = PotionConfigMod.WITCH_POTIONS_SPLASH.get(potionId);
        } else if (PotionConfigMod.WITCH_POTIONS_LINGERING.containsKey(potionId)) {
            potion = PotionConfigMod.WITCH_POTIONS_LINGERING.get(potionId);
            return PotionUtil.setPotion(new ItemStack(Items.LINGERING_POTION), potion);
        }
        return PotionUtil.setPotion(stack, potion);
    }
}
