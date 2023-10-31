package com.stratecide.potion_config;

import com.stratecide.potion_config.blocks.FloorBlock;
import com.stratecide.potion_config.blocks.FloorBlockModelProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PotionConfigClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        for (FloorBlock block : PotionConfigMod.FLOOR_BLOCKS.values()) {
            Identifier identifier = Registry.BLOCK.getId(block);
            ModelLoadingRegistry.INSTANCE.registerVariantProvider(rm -> new FloorBlockModelProvider(identifier.getPath()));
        }
    }
}
