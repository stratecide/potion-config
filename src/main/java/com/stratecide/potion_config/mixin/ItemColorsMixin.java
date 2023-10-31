package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.potion.PotionUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemColors.class)
public class ItemColorsMixin {

    @Inject(method = "create", at = @At("TAIL"))
    private static void injectCraftingPotionColors(BlockColors blockColors, CallbackInfoReturnable<ItemColors> cir) {
        cir.getReturnValue().register((stack, tintIndex) -> tintIndex > 0 ? -1 : PotionUtil.getColor(stack), PotionConfigMod.CRAFTING_POTION);
    }
}
