package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.CustomPotion;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(ItemGroups.class)
public abstract class ItemGroupsMixin {

    @Shadow
    protected static void addPotions(ItemGroup.Entries entries, RegistryWrapper<Potion> registryWrapper, Item item, ItemGroup.StackVisibility visibility) {
    }

    @Inject(method = "addPotions", at = @At("HEAD"), cancellable = true)
    private static void replacePotions(ItemGroup.Entries entries, RegistryWrapper<Potion> registryWrapper, Item item, ItemGroup.StackVisibility visibility, CallbackInfo ci) {
        registryWrapper.streamEntries()
                .filter(entry -> !entry.matchesKey(Potions.EMPTY_KEY))
                .filter(entry -> !PotionConfigMod.UNSTABLE_POTIONS.containsKey(entry.value()))
                .filter(potion -> PotionConfigMod.CUSTOM_POTIONS.containsKey(potion.value()))
                .map(entry -> PotionUtil.setPotion(new ItemStack(item), entry.value()))
                .filter(itemStack -> itemStack.isOf(item) && PotionUtil.getPotion(itemStack) != Potions.EMPTY)
                .forEach(stack -> entries.add(stack, visibility));
        if (item == Items.LINGERING_POTION)
            addPotions(entries, registryWrapper, PotionConfigMod.CRAFTING_POTION, visibility);
        ci.cancel();
    }
}
