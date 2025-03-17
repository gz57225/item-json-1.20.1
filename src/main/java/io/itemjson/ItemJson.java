package io.itemjson;

import io.itemjson.json.JsonReader;
import io.itemjson.manager.BlockManager;
import io.itemjson.manager.ItemManager;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ItemJson implements ModInitializer {
	public static final String MOD_ID = "itemjson";
	public static final Logger THE_LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final BlockManager BLOCK_MANAGER = new BlockManager(MOD_ID, ItemJson.class.getClassLoader());
    public static final ItemManager ITEM_MANAGER = new ItemManager(MOD_ID, ItemJson.class.getClassLoader());

	@Override
	public void onInitialize() {
		THE_LOGGER.info("Hello Fabric world!");
        try {
            BLOCK_MANAGER.registerAll();
            ITEM_MANAGER.registerAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}