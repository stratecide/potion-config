package com.stratecide.potion_config.mixin;

import com.google.common.collect.Lists;
import com.stratecide.potion_config.*;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(PotionUtil.class)
public abstract class PotionUtilMixin {

    @Inject(method = "getPotion(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/potion/Potion;", at = @At("HEAD"), cancellable = true)
    private static void allowOtherPotionTypes(ItemStack stack, CallbackInfoReturnable<Potion> cir) {
        if (PotionConfigMod.HONEY_BOTTLE_POTION != Potions.EMPTY && stack.isOf(Items.HONEY_BOTTLE))
            cir.setReturnValue(PotionConfigMod.HONEY_BOTTLE_POTION);
    }

    @Inject(method = "setPotion", at = @At("HEAD"), cancellable = true)
    private static void replacePotions(ItemStack stack, Potion potion, CallbackInfoReturnable<ItemStack> cir) {
        boolean changed = false;
        Map<Potion, Integer> options = PotionConfigMod.UNSTABLE_POTIONS.get(potion);
        if (options != null) {
            int space = options.values().stream().reduce(0, Integer::sum);
            int random = (int) (Math.random() * (double) space);
            for (Map.Entry<Potion, Integer> entry: options.entrySet()) {
                if (random < entry.getValue()) {
                    // replace unstable potion with a random output
                    potion = entry.getKey();
                    changed = true;
                    break;
                }
                random -= entry.getValue();
            }
        }
        Item item = stack.getItem();
        CustomPotion customPotion = PotionConfigMod.getCustomPotion(potion);
        if (potion != Potions.EMPTY && potion == PotionConfigMod.HONEY_BOTTLE_POTION) {
            cir.setReturnValue(new ItemStack(Items.HONEY_BOTTLE));
            return;
        } else if (item instanceof PotionItem) {
            item = customPotion.getPotionItem();
        } else if (item == Items.TIPPED_ARROW && potion != Potions.EMPTY && !PotionConfigMod.ARROW_POTIONS.contains(customPotion)) {
            potion = Potions.EMPTY;
            changed = true;
        }
        if (item != stack.getItem()) {
            changed = true;
            ItemStack old = stack;
            stack = new ItemStack(item, old.getCount());
            stack.setNbt(old.getNbt());
        }
        if (changed) {
            cir.setReturnValue(PotionUtil.setPotion(stack, potion));
        }
    }

    @Inject(method = "getPotionEffects(Lnet/minecraft/item/ItemStack;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private static void getPotionEffectsInject(ItemStack stack, CallbackInfoReturnable<List<StatusEffectInstance>> cir) {
        Potion vanillaPotion = PotionUtil.getPotion(stack);
        CustomPotion potion = PotionConfigMod.getCustomPotion(vanillaPotion);
        List<StatusEffectInstance> list = potion.generateEffectInstances();
        PotionUtil.getCustomPotionEffects(stack.getNbt(), list);
        cir.setReturnValue(list);
    }

    // TODO: other getPotionEffects methods

    @Inject(method = "getColor(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private static void getCustomColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Potion vanillaPotion = PotionUtil.getPotion(stack);
        CustomPotion potion = PotionConfigMod.getCustomPotion(vanillaPotion);
        cir.setReturnValue(potion.getColor(true));
    }

    // TODO: other getColor methods

    @Inject(method = "buildTooltip", at = @At("HEAD"), cancellable = true)
    private static void injectToolTip(ItemStack stack, List<Text> list, float durationMultiplier, CallbackInfo ci) {
        Potion vanillaPotion = PotionUtil.getPotion(stack);
        CustomPotion potion = PotionConfigMod.getCustomPotion(vanillaPotion);
        potion.buildToolTip(list, durationMultiplier);
        list.add(ScreenTexts.EMPTY);
        ci.cancel();
    }
}
