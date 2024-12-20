package net.asian.civiliansmod.registry;

import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EntityModelLayerRegistry {
    private static final Map<EntityModelLayer, Supplier<TexturedModelData>> MODEL_LAYERS = new HashMap<>();

    public static void registerModelLayer(EntityModelLayer layer, Supplier<TexturedModelData> supplier) {
        if (MODEL_LAYERS.put(layer, supplier) != null) {
            throw new IllegalStateException("Duplicate registration for entity model layer: " + getId(layer));
        }
    }

    public static TexturedModelData getModelData(EntityModelLayer layer) {
        Supplier<TexturedModelData> supplier = MODEL_LAYERS.get(layer);
        if (supplier == null) {
            throw new IllegalStateException("No model data found for layer: " + getId(layer));
        }
        return supplier.get();
    }

    private static Identifier getId(EntityModelLayer layer) {
        return Identifier.of(layer.getId().getNamespace(), "models/entity/" + layer.getId().getPath());
    }
}
