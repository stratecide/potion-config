package com.stratecide.potion_config.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.util.hit.HitResult;

@Mixin(PotionEntity.class)
public interface PotionEntityInvoker {
    @Invoker("onCollision")
    void invokeOnCollision(HitResult hitResult);
}
