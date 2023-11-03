package com.stratecide.potion_config.blocks.portal;

import com.stratecide.potion_config.PotionConfigMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class PortalBlockModelProvider implements ModelVariantProvider {
    private final String blockId;
    private final Map<Direction.Axis, PortalBlockModel> models = new HashMap<>();

    public PortalBlockModelProvider(String blockId) {
        for (Direction.Axis axis : Direction.Axis.VALUES)
            models.put(axis, new PortalBlockModel(blockId, axis));
        this.blockId = blockId;
    }

    @Override
    public @Nullable UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) {
        if (PotionConfigMod.MOD_ID.equals(modelId.getNamespace()) && modelId.getPath().equals(blockId)) {
            if (modelId.getVariant().contains("axis=x"))
                return models.get(Direction.Axis.X);
            else if (modelId.getVariant().contains("axis=y"))
                return models.get(Direction.Axis.Y);
            else
                return models.get(Direction.Axis.Z);
        } else {
            return null;
        }
    }
}
