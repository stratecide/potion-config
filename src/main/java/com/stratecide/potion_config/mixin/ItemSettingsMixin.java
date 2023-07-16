package com.stratecide.potion_config.mixin;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.Settings.class)
public interface ItemSettingsMixin {
    @Accessor("maxCount")
    int getMaxCount();
}
