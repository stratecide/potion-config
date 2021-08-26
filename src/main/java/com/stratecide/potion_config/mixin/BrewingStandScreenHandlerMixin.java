package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.FuelSlot;
import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandScreenHandler.class)
public abstract class BrewingStandScreenHandlerMixin extends ScreenHandler {

    protected BrewingStandScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/screen/PropertyDelegate;)V", at = @At("RETURN"))
    void injectFuelSlot(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate, CallbackInfo ci) {
        if (PotionConfigMod.FUELS != null) {
            Slot fuelSlot = null;
            for (Slot slot : slots)
                if (slot.getIndex() == 4) {
                    fuelSlot = slot;
                    break;
                }
            if (fuelSlot == null)
                throw new NullPointerException("Fuel slot for BrewingStandScreenHandler not found");
            Slot newFuelSlot = new FuelSlot(inventory, fuelSlot.getIndex(), fuelSlot.x, fuelSlot.y);
            newFuelSlot.id = fuelSlot.id;
            slots.set(slots.indexOf(fuelSlot), newFuelSlot);
        }
    }

    @Redirect(method = "transferSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/BrewingStandScreenHandler$FuelSlot;matches(Lnet/minecraft/item/ItemStack;)Z"))
    boolean redirectFuelSlotMatches(ItemStack stack) {
        return FuelSlot.matches(stack);
    }
}
