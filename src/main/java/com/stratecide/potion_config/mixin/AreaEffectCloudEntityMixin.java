package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AreaEffectCloudEntity.class)
public abstract class AreaEffectCloudEntityMixin {
    @Shadow public abstract void setPotion(Potion potion);

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    void correctPotion(NbtCompound nbt, CallbackInfo ci) {
        setPotion(PotionConfigMod.findCustomPotionFromNbt(nbt, PotionConfigMod.PREFIX_LINGERING));
    }
}
