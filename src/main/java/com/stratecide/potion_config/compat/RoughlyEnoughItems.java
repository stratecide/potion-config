package com.stratecide.potion_config.compat;

import com.stratecide.potion_config.CustomPotion;
import com.stratecide.potion_config.PotionConfigMod;
import com.stratecide.potion_config.blocks.floor.FloorBlockRecipeContainer;
import com.stratecide.potion_config.blocks.portal.PortalBlockRecipeContainer;
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
            ItemStack craftingPotion = (ItemStack) entry.getValue();
            Potion potion = PotionUtil.getPotion(craftingPotion);
            if (registeredPotions.add(potion)) {
                CustomPotion customPotion = PotionConfigMod.getCustomPotion(potion);
                if (PotionConfigMod.ARROW_POTIONS.contains(customPotion)) {
                    List<EntryIngredient> input = new ArrayList<>();
                    for (int i = 0; i < 4; i++)
                        input.add(arrowStack);
                    input.add(EntryIngredients.of(craftingPotion));
                    for (int i = 0; i < 4; i++)
                        input.add(arrowStack);
                    ItemStack outputStack = new ItemStack(Items.TIPPED_ARROW, 8);
                    PotionUtil.setPotion(outputStack, potion);
                    PotionUtil.setCustomPotionEffects(outputStack, PotionUtil.getCustomPotionEffects(craftingPotion));
                    EntryIngredient output = EntryIngredients.of(outputStack);
                    registry.add(new DefaultCustomDisplay(null, input, Collections.singletonList(output)));
                }
                FloorBlockRecipeContainer floorRecipe = PotionConfigMod.FLOOR_BLOCK_RECIPES.get(potion);
                if (floorRecipe != null) {
                    EntryIngredient ingredient = EntryIngredients.ofIngredient(floorRecipe.ingredient());
                    List<EntryIngredient> input = new ArrayList<>();
                    for (int i = 0; i < 4; i++)
                        input.add(ingredient);
                    input.add(EntryIngredients.of(craftingPotion));
                    for (int i = 0; i < 4; i++)
                        input.add(ingredient);
                    ItemStack outputStack = new ItemStack(PotionConfigMod.FLOOR_BLOCKS.get(potion), floorRecipe.outputCount());
                    PotionUtil.setPotion(outputStack, potion);
                    PotionUtil.setCustomPotionEffects(outputStack, PotionUtil.getCustomPotionEffects(craftingPotion));
                    EntryIngredient output = EntryIngredients.of(outputStack);
                    registry.add(new DefaultCustomDisplay(null, input, Collections.singletonList(output)));
                }
                PortalBlockRecipeContainer portalRecipe = PotionConfigMod.PORTAL_BLOCK_RECIPES.get(potion);
                if (portalRecipe != null) {
                    EntryIngredient ingredient = EntryIngredients.ofIngredient(portalRecipe.ingredient());
                    List<EntryIngredient> input = new ArrayList<>();
                    for (int i = 0; i < 4; i++)
                        input.add(ingredient);
                    input.add(EntryIngredients.of(craftingPotion));
                    for (int i = 0; i < 4; i++)
                        input.add(ingredient);
                    ItemStack outputStack = new ItemStack(PotionConfigMod.PORTAL_BLOCKS.get(potion), portalRecipe.outputCount());
                    PotionUtil.setPotion(outputStack, potion);
                    PotionUtil.setCustomPotionEffects(outputStack, PotionUtil.getCustomPotionEffects(craftingPotion));
                    EntryIngredient output = EntryIngredients.of(outputStack);
                    registry.add(new DefaultCustomDisplay(null, input, Collections.singletonList(output)));
                }
            }
        });
    }
}
