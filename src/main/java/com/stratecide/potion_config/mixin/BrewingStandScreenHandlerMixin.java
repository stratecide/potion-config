package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.FuelSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BrewingStandScreenHandler.class)
public abstract class BrewingStandScreenHandlerMixin extends ScreenHandler {

    protected BrewingStandScreenHandlerMixin(BrewingStandScreenHandlerMixin asdf) {
        super(null, 0);
    }

    @Redirect(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/screen/PropertyDelegate;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/BrewingStandScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;"))
    Slot injectFuelSlot(BrewingStandScreenHandler instance, Slot slot) {
        if (slot.getIndex() == 4) {
            return this.addSlot(new FuelSlot(slot.inventory, slot.getIndex(), slot.x, slot.y));
        }
        return this.addSlot(slot);
    }

    @Redirect(method = "transferSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/BrewingStandScreenHandler$FuelSlot;matches(Lnet/minecraft/item/ItemStack;)Z"))
    boolean redirectFuelSlotMatches(ItemStack stack) {
        return FuelSlot.matches(stack);
    }
}
