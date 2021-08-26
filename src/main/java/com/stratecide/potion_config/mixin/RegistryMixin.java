package com.stratecide.potion_config.mixin;

import com.mojang.serialization.Lifecycle;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.potion.Potion;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Registry.class)
public abstract class RegistryMixin {

    @Inject(method = "register(Lnet/minecraft/util/registry/Registry;Lnet/minecraft/util/Identifier;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    private static <V, T extends V> void blockPotionRegistration(Registry<V> registry, Identifier id, T entry, CallbackInfoReturnable<T> cir) {
        if (registry == Registry.POTION && !"empty".equals(id.getPath())) {
            if (!"minecraft".equals(id.getNamespace())) {
                PotionConfigMod.replaceModdedPotion(id, (Potion) entry);
            }
            cir.setReturnValue(entry);
        }
    }
}
