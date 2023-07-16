package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.effects.CustomStatusEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyConstant(method = "getBlockBreakingSpeed", constant = @Constant(floatValue = 5.0f))
    private float applyFinesse(float constant) {
        if (this.hasStatusEffect(CustomStatusEffect.FINESSE)) {
            // 5 -> 4 -> 3.4 -> 3 -> 2.7 -> 2.5 -> 2.333
            return 1.0f + (constant - 1.0f) * 3.0f / (4.0f + (float) this.getStatusEffect(CustomStatusEffect.FINESSE).getAmplifier());
        }
        return constant;
    }
}
