package com.stratecide.potion_config;

import com.stratecide.potion_config.blocks.floor.FloorBlock;
import com.stratecide.potion_config.blocks.floor.FloorBlockModelProvider;
import com.stratecide.potion_config.blocks.portal.PortalBlock;
import com.stratecide.potion_config.blocks.portal.PortalBlockModelProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PotionConfigClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        for (FloorBlock block : PotionConfigMod.FLOOR_BLOCKS.values()) {
            Identifier identifier = Registry.BLOCK.getId(block);
            ModelLoadingRegistry.INSTANCE.registerVariantProvider(rm -> new FloorBlockModelProvider(identifier.getPath()));
        }
        for (PortalBlock block : PotionConfigMod.PORTAL_BLOCKS.values()) {
            Identifier identifier = Registry.BLOCK.getId(block);
            ModelLoadingRegistry.INSTANCE.registerVariantProvider(rm -> new PortalBlockModelProvider(identifier.getPath()));
            BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getTranslucent());
        }
    }
}
