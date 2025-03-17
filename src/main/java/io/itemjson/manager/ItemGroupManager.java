package io.itemjson.manager;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;

public class ItemGroupManager extends AbstractManager {
    public ArrayList<ItemGroup> ITEM_GROUPS = new ArrayList<>();

    public ItemGroupManager(String mod_id, ClassLoader loader) {
        super(mod_id, "item_groups", loader);
    }

    public ItemGroup getThis(String name) {
        return getThis(Registries.ITEM_GROUP, name);
    }

    public static ItemGroup get(String name) {
        return get(Registries.ITEM_GROUP, name);
    }

    @Override
    public void registerAll() throws IOException {
        ArrayList<JsonObject> jsonObjects = READER.listJsons();
        ArrayList<ItemGroup> itemGroups = new ArrayList<>();
        for (var jsonObject : jsonObjects) {
            String name = jsonObject.get("name").getAsString();
            Item icon = ItemManager.get(jsonObject.get("icon").getAsString());
            itemGroups.add(Registry.register(
                    Registries.ITEM_GROUP,
                    RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(getModId(), name)),
                    FabricItemGroup.builder()
                            .icon(() -> new ItemStack(icon))
                            .displayName(Text.translatable("itemGroup." + name))
                            .entries((displayContext, entries) -> {
                                jsonObject.get("items").getAsJsonArray().asList().forEach(element -> {
                                    entries.add(Registries.ITEM.get(new Identifier(element.getAsString())));
                                });
                            })
                            .build()
            ));
        }
        ITEM_GROUPS = itemGroups;
    }
}
