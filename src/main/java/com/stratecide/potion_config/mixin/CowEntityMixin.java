package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import com.stratecide.potion_config.PotionType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(CowEntity.class)
public abstract class CowEntityMixin extends AnimalEntity {
    private CowEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    public void injectInteractMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.GLASS_BOTTLE) && !this.isBaby() && PotionConfigMod.MILK_POTION != null && PotionConfigMod.hasCustomPotion(PotionConfigMod.MILK_POTION, Optional.of(PotionType.Normal))) {
            Potion potion = PotionConfigMod.getOriginalPotion(PotionConfigMod.MILK_POTION, PotionType.Normal);
            player.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);
            ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, player, PotionUtil.setPotion(Items.POTION.getDefaultStack(), potion));
            player.setStackInHand(hand, itemStack2);
            cir.setReturnValue(ActionResult.success(this.getWorld().isClient));
        }
    }
}
