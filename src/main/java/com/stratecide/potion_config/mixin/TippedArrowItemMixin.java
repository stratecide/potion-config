package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.CustomPotion;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TippedArrowItem.class)
public abstract class TippedArrowItemMixin extends Item {

    public TippedArrowItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "getTranslationKey", at = @At("HEAD"), cancellable = true)
    void properTranslationKey(ItemStack stack, CallbackInfoReturnable<String> cir) {
        Potion potion = PotionUtil.getPotion(stack);
        String key = PotionConfigMod.getPotionKey(potion);
        cir.setReturnValue(this.getTranslationKey() + ".effect." + key);
    }

    /*@Inject(method = "appendStacks", at = @At("HEAD"), cancellable = true)
    void removeFromCreativeInventory(ItemGroup group, DefaultedList<ItemStack> stacks, CallbackInfo ci) {
        for (CustomPotion potion : PotionConfigMod.ARROW_POTIONS) {
            stacks.add(PotionUtil.setPotion(new ItemStack(this), Registries.POTION.get(potion.potionId)));
        }
        ci.cancel();
    }*/
}
