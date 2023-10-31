package com.stratecide.potion_config.compat;

import com.stratecide.potion_config.CustomPotion;
import com.stratecide.potion_config.PotionConfigMod;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoughlyEnoughItems implements REIClientPlugin {

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        EntryIngredient arrowStack = EntryIngredient.of(EntryStacks.of(Items.ARROW));
        ReferenceSet<Potion> registeredPotions = new ReferenceOpenHashSet<>();
        EntryRegistry.getInstance().getEntryStacks().filter(entry -> entry.getValueType() == ItemStack.class && entry.<ItemStack>castValue().getItem() == PotionConfigMod.CRAFTING_POTION).forEach(entry -> {
            ItemStack itemStack = (ItemStack) entry.getValue();
            Potion potion = PotionUtil.getPotion(itemStack);
            if (registeredPotions.add(potion)) {
                CustomPotion customPotion = PotionConfigMod.getCustomPotion(potion);
                if (PotionConfigMod.ARROW_POTIONS.contains(customPotion)) {
                    List<EntryIngredient> input = new ArrayList<>();
                    for (int i = 0; i < 4; i++)
                        input.add(arrowStack);
                    input.add(EntryIngredients.of(itemStack));
                    for (int i = 0; i < 4; i++)
                        input.add(arrowStack);
                    ItemStack outputStack = new ItemStack(Items.TIPPED_ARROW, 8);
                    PotionUtil.setPotion(outputStack, potion);
                    PotionUtil.setCustomPotionEffects(outputStack, PotionUtil.getCustomPotionEffects(itemStack));
                    EntryIngredient output = EntryIngredients.of(outputStack);
                    registry.add(new DefaultCustomDisplay(null, input, Collections.singletonList(output)));
                }
            }
        });
    }
}
