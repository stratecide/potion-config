package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.CustomPotion;
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
                return settings.maxCount(PotionConfigMod.STACK_SIZE).recipeRemainder(Items.GLASS_BOTTLE);
            } catch (RuntimeException e) {
                return settings.maxDamage(0);
            }
        }
        return settings;
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
    void onlyAddCorrectTypeCombinations(ItemGroup group, DefaultedList<ItemStack> stacks, CallbackInfo ci) {
        if (this.isIn(group)) {
            for (CustomPotion potion : PotionConfigMod.CUSTOM_POTIONS.values()) {
                if (potion.getPotionItem() == this) {
                    stacks.add(PotionUtil.setPotion(new ItemStack(this), Registry.POTION.get(potion.potionId)));
                }
            }
            ci.cancel();
        }
    }
}
