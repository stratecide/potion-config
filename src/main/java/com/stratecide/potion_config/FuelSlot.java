package com.stratecide.potion_config;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class FuelSlot extends Slot {
    public FuelSlot(Inventory inventory, int i, int j, int k) {
        super(inventory, i, j, k);
    }

    public boolean canInsert(ItemStack stack) {
        return matches(stack);
    }

    public static boolean matches(ItemStack stack) {
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return PotionConfigMod.FUELS.containsKey(id);
    }

    public int getMaxItemCount() {
        return 64;
    }
}

