package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;

import net.minecraft.potion.Potion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.nbt.NbtCompound;

@Mixin(ArrowEntity.class)
public class ArrowEntityMixin {

    @Shadow private Potion potion;

    @Inject(method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    private void injectGetPotion(@Nullable NbtCompound nbt, CallbackInfo ci) {
        this.potion = PotionConfigMod.findCustomPotionFromNbt(nbt, PotionConfigMod.PREFIX_ARROW);
    }
}
