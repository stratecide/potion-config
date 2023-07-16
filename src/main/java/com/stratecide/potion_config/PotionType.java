package com.stratecide.potion_config;

import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;

import java.util.Optional;

public enum PotionType {
    Normal,
    Splash,
    Lingering;

    public static PotionType from(PotionItem potion) {
        if (potion instanceof SplashPotionItem)
            return Splash;
        if (potion instanceof LingeringPotionItem)
            return Lingering;
        return Normal;
    }

    public static Optional<PotionType> parse(String str) {
        switch (str) {
            case "normal" -> {
                return Optional.of(Normal);
            }
            case "splash" -> {
                return Optional.of(Splash);
            }
            case "lingering" -> {
                return Optional.of(Lingering);
            }
            default -> {
                PotionConfigMod.LOGGER.warn("Unknown potion type '" + str + "'");
                return Optional.empty();
            }
        }
    }

    public ItemStack build(Potion potion) {
        Item item = switch (this) {
            case Normal -> Items.POTION;
            case Splash -> Items.SPLASH_POTION;
            case Lingering -> Items.LINGERING_POTION;
        };
        return PotionUtil.setPotion(new ItemStack(item), potion);
    }
}
