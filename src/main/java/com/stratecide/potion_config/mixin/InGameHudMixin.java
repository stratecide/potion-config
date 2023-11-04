package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.effects.CustomStatusEffect;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow private int scaledWidth;

    @Shadow private int scaledHeight;

    @Shadow protected abstract void renderHotbarItem(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed);

    @Shadow protected abstract PlayerEntity getCameraPlayer();

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void injectElytraAmplifierIcon(float tickDelta, DrawContext context, CallbackInfo ci) {
        PlayerEntity playerEntity = getCameraPlayer();
        if (playerEntity == null) {
            return;
        }
        StatusEffectInstance elytraEffect = playerEntity.getStatusEffect(CustomStatusEffect.ELYTRA);
        if (elytraEffect != null && elytraEffect.getAmplifier() > 0) {
            int n = 9;
            int x = scaledWidth / 2 - 90 + n * 20 + 2;
            int y = scaledHeight - 16 - 3;
            ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET);
            itemStack.setCount(elytraEffect.getAmplifier());
            renderHotbarItem(context, x, y, tickDelta, playerEntity, itemStack, 127);
        }
    }
}
