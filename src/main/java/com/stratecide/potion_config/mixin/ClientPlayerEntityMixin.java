package com.stratecide.potion_config.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.encryption.PlayerPublicKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @ModifyConstant(method = "updateNausea", constant = @Constant(floatValue = 1.0f))
    private float respectNauseaStrength(float constant) {
        int amplifier = 0;
        if (this.hasStatusEffect(StatusEffects.NAUSEA)) {
            amplifier = this.getStatusEffect(StatusEffects.NAUSEA).getAmplifier();
        }
        return Math.min(constant, (1.0f + (float) amplifier) / 4.0f);
    }
}
