package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.item.Item;
import net.minecraft.item.SplashPotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SplashPotionItem.class)
public class SplashPotionItemMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static Item.Settings increaseStackSize(Item.Settings settings) {
        if (PotionConfigMod.STACK_SIZE_SPLASH > 1)
            return settings.maxCount(PotionConfigMod.STACK_SIZE_SPLASH);
        else
            return settings.maxDamage(1);
    }
}
