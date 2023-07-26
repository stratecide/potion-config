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
    /*@Inject(method = "setPotion", at = @At("HEAD"), cancellable = true)
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
    }*/

    @Inject(method = "getPotionEffects(Lnet/minecraft/item/ItemStack;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private static void getPotionEffectsInject(ItemStack stack, CallbackInfoReturnable<List<StatusEffectInstance>> cir) {
        Potion vanillaPotion = PotionUtil.getPotion(stack);
        CustomPotion potion;
        if (stack.isOf(Items.SPLASH_POTION)) {
            potion = PotionConfigMod.getSplashPotion(vanillaPotion);
        } else if (stack.isOf(Items.LINGERING_POTION)) {
            potion = PotionConfigMod.getLingeringPotion(vanillaPotion);
        } else if (stack.isOf(Items.TIPPED_ARROW)) {
            potion = PotionConfigMod.getArrowPotion(vanillaPotion);
        } else {
            potion = PotionConfigMod.getNormalPotion(vanillaPotion);
        }
        List<StatusEffectInstance> list = potion.generateEffectInstances();
        PotionUtil.getCustomPotionEffects(stack.getNbt(), list);
        cir.setReturnValue(list);
    }

    // TODO: other getPotionEffects methods

    @Inject(method = "getColor(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private static void getCustomColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Potion vanillaPotion = PotionUtil.getPotion(stack);
        CustomPotion potion;
        if (stack.isOf(Items.SPLASH_POTION)) {
            potion = PotionConfigMod.getSplashPotion(vanillaPotion);
        } else if (stack.isOf(Items.LINGERING_POTION)) {
            potion = PotionConfigMod.getLingeringPotion(vanillaPotion);
        } else if (stack.isOf(Items.TIPPED_ARROW)) {
            potion = PotionConfigMod.getArrowPotion(vanillaPotion);
        } else {
            potion = PotionConfigMod.getNormalPotion(vanillaPotion);
        }
        cir.setReturnValue(potion.getColor(true));
    }

    // TODO: other getColor methods

    @Inject(method = "buildTooltip", at = @At("HEAD"), cancellable = true)
    private static void injectToolTip(ItemStack stack, List<Text> list, float durationMultiplier, CallbackInfo ci) {
        Potion vanillaPotion = PotionUtil.getPotion(stack);
        CustomPotion potion;
        if (stack.getItem() instanceof PotionItem) {
            potion = switch (PotionType.from((PotionItem) stack.getItem())) {
                case Normal -> PotionConfigMod.getNormalPotion(vanillaPotion);
                case Splash -> PotionConfigMod.getSplashPotion(vanillaPotion);
                case Lingering -> PotionConfigMod.getLingeringPotion(vanillaPotion);
            };
        } else {
            potion = PotionConfigMod.getArrowPotion(vanillaPotion);
        }
        potion.buildToolTip(list, durationMultiplier);
        list.add(ScreenTexts.EMPTY);

        if (stack.isOf(Items.TIPPED_ARROW)) {
            arrowIngredientTooltip(PotionConfigMod.getCustomArrowPotionId(vanillaPotion), list);
        } else {
            PotionType type = PotionType.from((PotionItem) stack.getItem());
            ingredientTooltip(PotionConfigMod.getCustomPotionId(type, vanillaPotion), type, list);
        }
        ci.cancel();
    }

    private static void arrowIngredientTooltip(String outputPotion, List<Text> list) {
        if (outputPotion == null)
            return;
        if (PotionConfigMod.ARROW_INPUTS.containsKey(outputPotion)) {
            list.add(Text.translatable("potion-config.potion.arrowRecipe").formatted(Formatting.DARK_AQUA));
            List<ArrowInput> inputs = PotionConfigMod.ARROW_INPUTS.get(outputPotion);
            ArrowInput input = inputs.get((int) ((new Date().getTime() / PotionConfigMod.TOOLTIP_MILLISECONDS) % inputs.size()));
            ItemStack inputStack = input.inputType().build(input.potion());
            list.add(Text.translatable(inputStack.getTranslationKey()).formatted(Formatting.GRAY));
        } else {
            list.add(Text.translatable("potion-config.potion.notCraftable").formatted(Formatting.DARK_AQUA));
        }
    }

    private static void ingredientTooltip(String outputPotion, PotionType type, List<Text> list) {
        if (outputPotion == null)
            return;
        Map<String, List<PotionInput>> map = switch (type) {
            case Normal -> PotionConfigMod.NORMAL_INPUTS;
            case Splash -> PotionConfigMod.SPLASH_INPUTS;
            case Lingering -> PotionConfigMod.LINGERING_INPUTS;
        };
        if (map.containsKey(outputPotion)) {
            list.add(Text.translatable("potion-config.potion.potionRecipe").formatted(Formatting.DARK_AQUA));
            List<PotionInput> inputs = map.get(outputPotion);
            PotionInput input = inputs.get((int) ((new Date().getTime() / PotionConfigMod.TOOLTIP_MILLISECONDS) % inputs.size()));
            ItemStack potionStack = input.inputType().build(input.potion());
            list.add(Text.translatable(potionStack.getTranslationKey()).formatted(Formatting.GRAY));
            list.add(Text.translatable(new ItemStack(input.ingredient()).getTranslationKey()).formatted(Formatting.GRAY));
        } else {
            list.add(Text.translatable("potion-config.potion.notBrewable").formatted(Formatting.DARK_AQUA));
        }
    }
}
