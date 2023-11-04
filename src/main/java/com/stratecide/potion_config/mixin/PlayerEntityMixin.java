package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.effects.CustomStatusEffect;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow public abstract void startFallFlying();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("TAIL"), cancellable = true)
    private void applyFinesse(BlockState block, CallbackInfoReturnable<Float> cir) {
        if (this.hasStatusEffect(CustomStatusEffect.FINESSE)) {
            int count = 0;
            if (this.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this))
                count += 1;
            if (!this.isOnGround())
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

    @Inject(method = "checkFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"), cancellable = true)
    private void injectElytraEffect(CallbackInfoReturnable<Boolean> cir) {
        if (hasStatusEffect(CustomStatusEffect.ELYTRA)) {
            startFallFlying();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "checkFallFlying", at = @At("HEAD"), cancellable = true)
    private void injectElytraEffectRockets(CallbackInfoReturnable<Boolean> cir) {
        if (this.isFallFlying() && getStatusEffect(CustomStatusEffect.ELYTRA).getAmplifier() > 0) {
            if (!getWorld().isClient) {
                ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET);
                FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(getWorld(), itemStack, this);
                getWorld().spawnEntity(fireworkRocketEntity);
            }
            StatusEffectInstance oldInstance = getStatusEffect(CustomStatusEffect.ELYTRA);
            StatusEffectInstance instance = new StatusEffectInstance(CustomStatusEffect.ELYTRA, oldInstance.getDuration(), oldInstance.getAmplifier() - 1, oldInstance.isAmbient(), oldInstance.shouldShowParticles(), oldInstance.shouldShowIcon());
            if (oldInstance.hiddenEffect != null) {
                instance.upgrade(oldInstance.hiddenEffect);
            }
            setStatusEffect(instance, this);
            cir.setReturnValue(true);
        }
    }
}
