package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin {

    @Redirect(method = "getDefaultStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtil;setPotion(Lnet/minecraft/item/ItemStack;Lnet/minecraft/potion/Potion;)Lnet/minecraft/item/ItemStack;"))
    ItemStack replaceWater(ItemStack stack, Potion potion) {
        return PotionUtil.setPotion(stack, PotionConfigMod.WATER_POTION);
    }

    @Redirect(method = "appendStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;iterator()Ljava/util/Iterator;"))
    private Iterator<Potion> redirectPotionIterator(DefaultedRegistry defaultedRegistry) {
        if (((Object) this) instanceof SplashPotionItem)
            return PotionConfigMod.SPLASH_POTIONS.iterator();
        if (((Object) this) instanceof LingeringPotionItem)
            return PotionConfigMod.LINGERING_POTIONS.iterator();
        return PotionConfigMod.NORMAL_POTIONS.iterator();
    }
}
