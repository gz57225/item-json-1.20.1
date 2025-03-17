package io.itemjson.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.itemjson.manager.misc.TooltippedBlockItem;
import io.itemjson.manager.misc.TooltippedItem;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiConsumer;

public class ItemManager extends AbstractManager {
    public ArrayList<Item> ITEMS = new ArrayList<>();

    public ItemManager(String mod_id, ClassLoader loader) {
        super(mod_id, "items", loader);
    }

    private static final HashMap<String, BiConsumer<Item.Settings, JsonElement>> ELEMENT_TO_FUNC_MAP = new HashMap<>();
    private static final HashSet<String> IGNORED_ELEMENT = new HashSet<>(Set.of("name", "class", "from_block", "fuel_time", "composting_chance", "tooltips"));
    private static final HashMap<String, BiConsumer<FoodComponent.Builder, JsonElement>> FOOD_ELEMENT_TO_FUNC_MAP = new HashMap<>();

    static {
        ELEMENT_TO_FUNC_MAP.put("fireproof", (settings, element) -> {
            if (element.getAsBoolean())
                settings.fireproof();
        });
        ELEMENT_TO_FUNC_MAP.put("rarity", (settings, element) -> {
            switch (element.getAsString()) {
                case "uncommon": settings.rarity(Rarity.UNCOMMON); break;
                case "rare": settings.rarity(Rarity.RARE); break;
                case "epic": settings.rarity(Rarity.EPIC); break;
                default: settings.rarity(Rarity.COMMON); break;
            }
        });
        ELEMENT_TO_FUNC_MAP.put("max_count", (settings, element) -> {
            settings.maxCount(element.getAsInt());
        });
        ELEMENT_TO_FUNC_MAP.put("max_damage", (settings, element) -> {
            settings.maxDamage(element.getAsInt());
        });
        ELEMENT_TO_FUNC_MAP.put("recipe_remainder", (settings, element) -> {
            settings.recipeRemainder(Registries.ITEM.get(new Identifier(element.getAsString())));
        });

        FOOD_ELEMENT_TO_FUNC_MAP.put("always_edible", (food, element) -> {
            if (element.getAsBoolean()) food.alwaysEdible();
        });
        FOOD_ELEMENT_TO_FUNC_MAP.put("hunger", (food, element) -> {
            food.hunger(element.getAsInt());
        });
        FOOD_ELEMENT_TO_FUNC_MAP.put("saturation_modifier", (food, element) -> {
            food.saturationModifier(element.getAsFloat());
        });
        FOOD_ELEMENT_TO_FUNC_MAP.put("meat", (food, element) -> {
            if (element.getAsBoolean()) food.meat();
        });
        FOOD_ELEMENT_TO_FUNC_MAP.put("snake", (food, element) -> {
            if (element.getAsBoolean()) food.snack();
        });
        FOOD_ELEMENT_TO_FUNC_MAP.put("status_effect", (food, element) -> {
            for (var effect : element.getAsJsonArray()) {
                JsonObject effectObj = effect.getAsJsonObject();
                food.statusEffect(
                        new StatusEffectInstance(
                                Registries.STATUS_EFFECT.get(new Identifier(effectObj.get("effect").getAsString())),
                                effectObj.get("duration").getAsInt(),
                                effectObj.has("amplifier") ? effectObj.get("amplifier").getAsInt() : 0),
                        effectObj.has("chance") ? effectObj.get("chance").getAsFloat() : 1.0f
                );
            }
        });

        ELEMENT_TO_FUNC_MAP.put("food", (settings, element) -> {
            FoodComponent.Builder food = new FoodComponent.Builder();
            element.getAsJsonObject().asMap().forEach((name, subelement) -> {
                FOOD_ELEMENT_TO_FUNC_MAP.get(name).accept(food, subelement);
            });
            settings.food(food.build());
        });
    }

    public Item getThis(String name) {
        return getThis(Registries.ITEM, name);
    }

    public static Item get(String name) {
        return get(Registries.ITEM, name);
    }

    /**
     * @implNote Attribute priority: {@code class} > {@code from_block} = {@code tooltip} <br/>
     * This means that once a high priority attribute appears, the lower priority attributes will not take effect. <br/>
     * Also, register {@code Block} before registering {@code BlockItem}!
     */
    public void registerAll() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ArrayList<JsonObject> jsonObjects = READER.listJsons();
        ArrayList<Item> items = new ArrayList<>();
        for (var jsonObject : jsonObjects) {
            Pair<Item, String> info = parse(jsonObject);
            Item item = Registry.register(Registries.ITEM, Identifier.of(getModId(), info.getRight()), info.getLeft());
            items.add(item);
            if (jsonObject.has("fuel_time")) {
                FuelRegistry.INSTANCE.add(item, jsonObject.get("fuel_time").getAsInt());
            }
            if (jsonObject.has("composting_chance")) {
                CompostingChanceRegistry.INSTANCE.add(item, jsonObject.get("composting_chance").getAsFloat());
            }
        }
        ITEMS = items;
    }

    protected Pair<Item, String> parse(JsonObject jsonObject) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String name = jsonObject.get("name").getAsString();
        Item.Settings settings = new Item.Settings();

        for (Map.Entry<String, JsonElement> entry :
                jsonObject.asMap().entrySet().stream().filter(e_entry -> !IGNORED_ELEMENT.contains(e_entry.getKey())).toList()) {
            ELEMENT_TO_FUNC_MAP.get(entry.getKey()).accept(settings, entry.getValue());
        }

        if (jsonObject.has("class")) {
            String clazz = jsonObject.get("class").getAsString();
            return new Pair<>(newClassByPath(clazz, settings), name);
        }

        if (jsonObject.has("from_block") && jsonObject.has("tooltips")) {
            var TOOLTIPS = getTooltips(jsonObject);
            return new Pair<>(new TooltippedBlockItem(Registries.BLOCK.get(new Identifier(jsonObject.get("from_block").getAsString())), settings, TOOLTIPS), name);
        }
        if (jsonObject.has("from_block")) {
            return new Pair<>(new BlockItem(Registries.BLOCK.get(new Identifier(jsonObject.get("from_block").getAsString())), settings), name);
        }
        if (jsonObject.has("tooltips")) {
            var TOOLTIPS = getTooltips(jsonObject);
            return new Pair<>(new TooltippedItem(settings, TOOLTIPS), name);
        }

        return new Pair<>(new Item(settings), name);
    }

    private static @NotNull ArrayList<Text> getTooltips(JsonObject jsonObject) {
        var TOOLTIPS = new ArrayList<Text>();
        jsonObject.get("tooltips").getAsJsonArray().forEach(element -> {
            JsonObject obj = element.getAsJsonObject();
            String text = obj.get("text").getAsString();
            String type = obj.has("type") ? obj.get("type").getAsString() : "literal";

            TOOLTIPS.add(type.equals("translatable") ? Text.translatable(text) : Text.literal(text));
        });
        return TOOLTIPS;
    }

    protected Item newClassByPath(String path, Item.Settings settings) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return (Item) newClassByPath(path, Item.Settings.class, settings);
    }
}
