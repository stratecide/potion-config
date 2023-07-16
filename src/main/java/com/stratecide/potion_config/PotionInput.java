package com.stratecide.potion_config;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;

public record PotionInput(Potion potion, PotionType inputType, Item ingredient) {
}
