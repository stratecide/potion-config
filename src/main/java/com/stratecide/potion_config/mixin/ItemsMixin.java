package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;

import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Items.class)
public abstract class ItemsMixin {

    @ModifyVariable(method = "register(Ljava/lang/String;Lnet/minecraft/item/Item;)Lnet/minecraft/item/Item;", at = @At("HEAD"), ordinal = 0)
    private static Item setPotionStackSize(Item item) {
        if (item instanceof PotionItem && !(item instanceof SplashPotionItem) && !(item instanceof LingeringPotionItem)) {
            return new PotionItem((new Item.Settings()).maxCount(PotionConfigMod.STACK_SIZE).group(ItemGroup.BREWING));
        }
        return item;
    }
}
