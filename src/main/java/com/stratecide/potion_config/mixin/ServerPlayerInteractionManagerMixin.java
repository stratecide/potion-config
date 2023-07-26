package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.effects.CreativeFlight;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow @Final protected ServerPlayerEntity player;
    private boolean wasFlying = false;

    @Inject(method = "setGameMode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameMode;setAbilities(Lnet/minecraft/entity/player/PlayerAbilities;)V"))
    private void saveFlyingState(GameMode gameMode, GameMode previousGameMode, CallbackInfo ci) {
        wasFlying = player.getAbilities().flying;
    }
    @Inject(method = "changeGameMode", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;sendAbilitiesUpdate()V"))
    private void injectCreativeFlight(GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
        if (gameMode != GameMode.CREATIVE && gameMode != GameMode.SPECTATOR) {
            for (StatusEffectInstance statusEffectInstance : player.getStatusEffects()) {
                if (statusEffectInstance.getEffectType() instanceof CreativeFlight) {
                    player.getAbilities().allowFlying = true;
                    player.getAbilities().flying = wasFlying;
                    break;
                }
            }
        }
    }
}
