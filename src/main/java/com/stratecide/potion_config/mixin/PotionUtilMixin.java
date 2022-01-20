package com.stratecide.potion_config.mixin;

import com.google.common.collect.Lists;
import com.stratecide.potion_config.ModdedBrewingRecipeRegistry;
import com.stratecide.potion_config.PotionConfigMod;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PotionUtil.class)
public abstract class PotionUtilMixin {
    @Inject(method = "setPotion", at = @At("HEAD"), cancellable = true)
    private static void replacePotions(ItemStack stack, Potion potion, CallbackInfoReturnable<ItemStack> cir) {
        if (potion == Potions.WATER || PotionConfigMod.MOD_COMPAT.containsKey(potion)) {
            if (potion == Potions.WATER)
                potion = PotionConfigMod.WATER_POTION;
            else
                potion = Registry.POTION.get(PotionConfigMod.MOD_COMPAT.get(potion));
        }
        Identifier identifier = Registry.POTION.getId(potion);
        Identifier vanillaIdentifier = PotionConfigMod.CUSTOM_TO_VANILLA.get(identifier);
        if (vanillaIdentifier == null)
            stack.removeSubNbt(PotionUtil.POTION_KEY);
        else
            stack.getOrCreateNbt().putString(PotionUtil.POTION_KEY, vanillaIdentifier.toString());
        stack.getOrCreateNbt().putString(PotionConfigMod.NBT_KEY, identifier.toString());
        cir.setReturnValue(stack);
}

    @Inject(method = "getPotionEffects(Lnet/minecraft/item/ItemStack;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private static void injectGetPotionEffects(@Nullable ItemStack stack, CallbackInfoReturnable<List<StatusEffectInstance>> cir) {
        Potion potion = PotionConfigMod.findCustomPotionFromStack(stack);
        ArrayList<StatusEffectInstance> list = Lists.newArrayList();
        list.addAll(potion.getEffects());
        PotionUtil.getCustomPotionEffects(stack.getNbt(), list);
        cir.setReturnValue(list);
    }

    @Inject(method = "getPotion(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/potion/Potion;", at = @At("HEAD"), cancellable = true)
    private static void injectGetPotion(@Nullable ItemStack stack, CallbackInfoReturnable<Potion> cir) {
        cir.setReturnValue(PotionConfigMod.findCustomPotionFromStack(stack));
    }

    @Inject(method = "getPotion(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/potion/Potion;", at = @At("HEAD"), cancellable = true)
    private static void injectGetPotion(@Nullable NbtCompound compound, CallbackInfoReturnable<Potion> cir) {
        cir.setReturnValue(PotionConfigMod.findCustomPotionFromNbt(compound, null));
    }

    @Inject(method = "buildTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 0))
    private static void injectToolTip(ItemStack stack, List<Text> list, float f, CallbackInfo ci) {
        Potion potion = PotionUtil.getPotion(stack);
        if (stack.isOf(Items.TIPPED_ARROW))
            ModdedBrewingRecipeRegistry.arrowIngredientTooltip(potion, list);
        else
            ModdedBrewingRecipeRegistry.ingredientTooltip(potion, list);
    }
}
