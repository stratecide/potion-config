package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.potion.Potion;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

@Mixin(TippedArrowItem.class)
public abstract class TippedArrowItemMixin {

    @Redirect(method = "appendStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;iterator()Ljava/util/Iterator;"))
    private Iterator<Potion> redirectPotionIterator(DefaultedRegistry defaultedRegistry) {
        return PotionConfigMod.ARROW_POTIONS.iterator();
    }
}
