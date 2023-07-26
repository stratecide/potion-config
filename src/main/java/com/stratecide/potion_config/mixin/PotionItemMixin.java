package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;

import com.stratecide.potion_config.PotionType;
import net.minecraft.block.DirtPathBlock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin extends Item {
    public PotionItemMixin(Settings settings) {
        super(settings);
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static Item.Settings increaseStackSize(Item.Settings settings) {
        if (((ItemSettingsMixin) settings).getMaxCount() == 1) {
            try {
                return settings.maxCount(PotionConfigMod.STACK_SIZE);
            } catch (RuntimeException e) {
                return settings.maxDamage(0);
            }
        }
        return settings;
    }

    @Inject(method = "getTranslationKey", at = @At("HEAD"), cancellable = true)
    void properTranslationKey(ItemStack stack, CallbackInfoReturnable<String> cir) {
        Potion potion = PotionUtil.getPotion(stack);
        Identifier identifier = Registry.POTION.getId(potion);
        PotionType type = PotionType.from((PotionItem) (Object) this);
        boolean exists = switch (type) {
            case Normal -> PotionConfigMod.hasNormalPotion(potion);
            case Splash -> PotionConfigMod.hasSplashPotion(potion);
            case Lingering -> PotionConfigMod.hasLingeringPotion(potion);
        };
        cir.setReturnValue(this.getTranslationKey() + ".effect." + (exists ? identifier.getPath() : "mystery"));
    }

    @Redirect(method = "finishUsing", at=@At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"))
    boolean dropBottleIfNotEnoughInventory(PlayerInventory playerInventory, ItemStack stack) {
        if (!playerInventory.insertStack(stack)) {
            playerInventory.player.dropItem(stack, false);
        }
        return false;
    }

    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    void injectHasGlint(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!PotionConfigMod.GLINT)
            cir.setReturnValue(false);
    }

    @Inject(method = "appendStacks", at = @At("HEAD"), cancellable = true)
    void removeFromCreativeInventory(ItemGroup group, DefaultedList<ItemStack> stacks, CallbackInfo ci) {
        if (this.isIn(group)) {
            PotionItem self = (PotionItem) ((Object) this);
            if (self instanceof SplashPotionItem) {
                for (Potion potion : Registry.POTION) {
                    if (potion != Potions.EMPTY && PotionConfigMod.hasSplashPotion(potion)) {
                        stacks.add(PotionUtil.setPotion(new ItemStack(this), potion));
                    }
                }
            } else if (self instanceof LingeringPotionItem) {
                for (Potion potion : Registry.POTION) {
                    if (potion != Potions.EMPTY && PotionConfigMod.hasLingeringPotion(potion)) {
                        stacks.add(PotionUtil.setPotion(new ItemStack(this), potion));
                    }
                }
            } else {
                for (Potion potion : Registry.POTION) {
                    if (potion != Potions.EMPTY && PotionConfigMod.hasNormalPotion(potion)) {
                        stacks.add(PotionUtil.setPotion(new ItemStack(this), potion));
                    }
                }
            }
            ci.cancel();
        }
    }
}
