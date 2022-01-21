package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "applyDamage", at = @At("RETURN"))
    void injectApplyDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (amount > 0 && !this.isInvulnerableTo(source) && ((Entity) this) instanceof ServerPlayerEntity) {
            if (PotionConfigMod.BURST_CHANCE > 0 && (DamageSource.FALL == source || DamageSource.CACTUS == source ||
                    DamageSource.FLY_INTO_WALL == source || DamageSource.ANVIL == source || DamageSource.FALLING_BLOCK == source ||
                    source instanceof EntityDamageSource && !source.isMagic())) {
                ServerPlayerEntity self = (ServerPlayerEntity) (Entity) this;
                for(int i = 0; i < self.getInventory().size(); ++i) {
                    ItemStack itemStack = self.getInventory().getStack(i);
                    if (!itemStack.isEmpty() && (itemStack.getItem() == Items.POTION || itemStack.getItem() == Items.SPLASH_POTION || itemStack.getItem() == Items.LINGERING_POTION) && random.nextFloat() < PotionConfigMod.BURST_CHANCE) {
                        PotionEntity potionEntity = new PotionEntity(world, self);
                        potionEntity.setItem(itemStack);
                        potionEntity.setVelocity(self, self.getPitch(), self.getYaw(), -20.0F, 0.5F, 1.0F);
                        world.spawnEntity(potionEntity);
                        PotionEntityInvoker pot = (PotionEntityInvoker) potionEntity;
                        pot.invokeOnCollision(new EntityHitResult(self));
                        itemStack.decrement(1);
                    }
                }
            }
        }
    }
}
