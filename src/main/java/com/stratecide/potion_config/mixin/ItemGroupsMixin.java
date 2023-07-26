package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(ItemGroups.class)
public class ItemGroupsMixin {
    @Inject(method = "addPotions", at = @At("HEAD"), cancellable = true)
    private static void replacePotions(ItemGroup.Entries entries, RegistryWrapper<Potion> registryWrapper, Item item, ItemGroup.StackVisibility visibility, CallbackInfo ci) {
        Function<Potion, Boolean> test = switch (Registries.ITEM.getId(item).getPath()) {
            case "potion" -> PotionConfigMod::hasNormalPotion;
            case "splash_potion" -> PotionConfigMod::hasSplashPotion;
            case "lingering_potion" -> PotionConfigMod::hasLingeringPotion;
            case "tipped_arrow" -> PotionConfigMod::hasArrowPotion;
            default -> null;
        };
        if (test != null) {
            registryWrapper.streamEntries()
                    .filter(entry -> !entry.matchesKey(Potions.EMPTY_KEY))
                    .filter(entry -> test.apply(entry.value()))
                    .map(entry -> PotionUtil.setPotion(new ItemStack(item), entry.value()))
                    .forEach(stack -> entries.add(stack, visibility));
            ci.cancel();
        }
    }
}
