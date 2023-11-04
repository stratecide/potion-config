package com.stratecide.potion_config;

import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;

import java.util.Optional;

public enum PotionType {
    Normal,
    Splash,
    Lingering,
    CraftIngredient;

    /*public static PotionType from(PotionItem potion) {
        if (potion instanceof SplashPotionItem)
            return Splash;
        if (potion instanceof LingeringPotionItem)
            return Lingering;
        return Normal;
    }

    public ItemStack build(Potion potion) {
        Item item = switch (this) {
            case Normal -> Items.POTION;
            case Splash -> Items.SPLASH_POTION;
            case Lingering -> Items.LINGERING_POTION;
        };
        return PotionUtil.setPotion(new ItemStack(item), potion);
    }*/
}
