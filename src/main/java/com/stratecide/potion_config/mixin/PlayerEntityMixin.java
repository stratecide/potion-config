package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.effects.CustomStatusEffect;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.FluidTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("TAIL"), cancellable = true)
    private void applyFinesse(BlockState block, CallbackInfoReturnable<Float> cir) {
        if (this.hasStatusEffect(CustomStatusEffect.FINESSE)) {
            int count = 0;
            if (this.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this))
                count += 1;
            if (!this.onGround)
                count += 1;
            double result = cir.getReturnValue();
            for (int i = 0; i < count; i++) {
                result *= 5.0;
                // 5 -> 4 -> 3.4 -> 3 -> 2.7 -> 2.5 -> 2.333
                result /= 1.0 + 12.0 / (4.0 + (double) this.getStatusEffect(CustomStatusEffect.FINESSE).getAmplifier());
            }
            cir.setReturnValue((float) result);
        }
    }
}
