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
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile, @Nullable PlayerPublicKey publicKey) {
        super(world, profile, publicKey);
    }

    @ModifyConstant(method = "updateNausea", constant = @Constant(floatValue = 1.0f, ordinal = 2))
    private float respectNauseaStrength(float constant) {
        return Math.min(constant, (1.0f + (float) this.getStatusEffect(StatusEffects.NAUSEA).getAmplifier()) / 4.0f);
    }
    @ModifyConstant(method = "updateNausea", constant = @Constant(floatValue = 1.0f, ordinal = 3))
    private float respectNauseaStrength1(float constant) {
        return respectNauseaStrength(constant);
    }
}
