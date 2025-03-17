package io.itemjson.client.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.itemjson.ItemJson;
import io.itemjson.json.JsonReader;
import io.itemjson.manager.BlockManager;
import io.itemjson.manager.ItemManager;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ModelGenerator {
    public static void generateBlockStateModels(BlockStateModelGenerator gen, ClassLoader loader, BlockManager manager) {
        JsonReader reader = JsonReader.of(manager.getModId(), "blocks/textures", loader);
        try {
            JsonObject jsonObject = reader.getJson("blocks.json");
            var simpleCubeAllBlocks = jsonObject.get("simple_cube_all").getAsJsonArray();
            for (var block : simpleCubeAllBlocks) {
                gen.registerSimpleCubeAll(BlockManager.get(block.getAsString()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final HashMap<String, BiConsumer<JsonArray, ItemModelGenerator>> ITEM_MODELS_TYPES = new HashMap<>();

    static {
        ITEM_MODELS_TYPES.put("generated", (array, gen) -> {
            for (var item : array) {
                JsonObject obj = item.getAsJsonObject();
                if (obj.has("texture")) {
                    gen.register(ItemManager.get(obj.get("name").getAsString()), ItemManager.get(obj.get("texture").getAsString()), Models.GENERATED);
                } else {
                    gen.register(ItemManager.get(obj.get("name").getAsString()), Models.GENERATED);
                }
            }
        });
        ITEM_MODELS_TYPES.put("handheld", (array, gen) -> {
            for (var item : array) {
                JsonObject obj = item.getAsJsonObject();
                if (obj.has("texture")) {
                    gen.register(ItemManager.get(obj.get("name").getAsString()), ItemManager.get(obj.get("texture").getAsString()), Models.HANDHELD);
                } else {
                    gen.register(ItemManager.get(obj.get("name").getAsString()), Models.HANDHELD);
                }
            }
        });
    }

    public static void generateItemModels(ItemModelGenerator gen, ClassLoader loader, ItemManager manager) {
        JsonReader reader = JsonReader.of(manager.getModId(), "items/textures", loader);
        try {
            JsonObject jsonObject = reader.getJson("items.json");
            for (Map.Entry<String, BiConsumer<JsonArray, ItemModelGenerator>> elementName : ITEM_MODELS_TYPES.entrySet()) {
                if (jsonObject.has(elementName.getKey())) {
                    elementName.getValue().accept(jsonObject.get(elementName.getKey()).getAsJsonArray(), gen);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
