package com.stratecide.potion_config.mixin;

import com.stratecide.potion_config.PotionConfigMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin extends BlockEntity {

    @Shadow private int brewTime;

    public BrewingStandBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Accessor("fuel")
    abstract int getFuel();
    @Accessor("fuel")
    abstract void setFuel(int fuel);

    @Accessor("inventory")
    abstract DefaultedList<ItemStack> getInventory();

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    void injectIsValid(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot == 4) {
            // fuel
            Identifier id = Registry.ITEM.getId(stack.getItem());
            cir.setReturnValue(PotionConfigMod.FUELS.containsKey(id));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private static void injectTick(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
        BrewingStandBlockEntityMixin entity = (BrewingStandBlockEntityMixin) (BlockEntity) blockEntity;
        if (entity.getFuel() <= 0 && entity.brewTime <= 0) {
            ItemStack itemStack = entity.getInventory().get(4);
            Identifier id = Registry.ITEM.getId(itemStack.getItem());
            if (PotionConfigMod.FUELS.containsKey(id)) {
                entity.setFuel(PotionConfigMod.FUELS.get(id));
                itemStack.decrement(1);
                markDirty(world, pos, state);
            }
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;get(I)Ljava/lang/Object;"))
    private static <E> E blockDefaultFuel(DefaultedList<E> defaultedList, int index) {
        if (index == 4) {
            return (E) ItemStack.EMPTY;
        }
        return defaultedList.get(index);
    }
}
