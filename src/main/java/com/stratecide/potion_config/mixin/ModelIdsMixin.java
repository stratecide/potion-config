package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.CraftingPotion;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.data.client.ModelIds;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelIds.class)
public class ModelIdsMixin {
    @Inject(method = "getItemModelId", at = @At("HEAD"), cancellable = true)
    static void craftingPotionModel(Item item, CallbackInfoReturnable<Identifier> cir) {
        if (item instanceof CraftingPotion) {
            cir.setReturnValue(new Identifier(PotionConfigMod.MOD_ID, "item/crafting_potion"));
        } else if (item instanceof TippedArrowItem && item != Items.TIPPED_ARROW) {
            cir.setReturnValue(ModelIds.getItemModelId(Items.TIPPED_ARROW));
        }
    }
}
