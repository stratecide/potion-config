package com.stratecide.potion_config.blocks;

import com.stratecide.potion_config.PotionConfigMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class FloorBlockModelProvider implements ModelVariantProvider {
    private final String blockId;
    private final FloorBlockModel model;

    public FloorBlockModelProvider(String blockId) {
        this.model = new FloorBlockModel(blockId);
        this.blockId = blockId;
    }

    @Override
    public @Nullable UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) throws ModelProviderException {
        if (PotionConfigMod.MOD_ID.equals(modelId.getNamespace()) && modelId.getPath().equals(blockId)) {
            return model;
        } else {
            return null;
        }
    }
}
