package io.itemjson.manager;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
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
    public static final ArrayList<ItemGroup> ITEM_GROUPS = new ArrayList<>();

    public ItemGroupManager(String mod_id) {
        super(mod_id, "item_groups");
    }

    @Override
    public ArrayList<ItemGroup> registerAll() throws IOException {
        ArrayList<JsonObject> jsonObjects = READER.listJsons();
        ArrayList<ItemGroup> itemGroups = new ArrayList<>();
        for (var jsonObject : jsonObjects) {
            String name = jsonObject.get("name").getAsString();
            Item icon = Registries.ITEM.get(new Identifier(jsonObject.get("icon").getAsString()));
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
        ITEM_GROUPS.addAll(itemGroups);
        return ITEM_GROUPS;
    }
}
