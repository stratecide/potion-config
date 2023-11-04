package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.potion.Potion;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Potion.class)
public class PotionMixin {
    @Inject(method = "finishTranslationKey", at = @At("HEAD"), cancellable = true)
    void useConfiguredKeys(String prefix, CallbackInfoReturnable<String> cir) {
        String key = PotionConfigMod.CUSTOM_IDS_REVERSE.get(Registry.POTION.getId((Potion) (Object) this));
        if (key != null)
            cir.setReturnValue(prefix + key);
    }
}
