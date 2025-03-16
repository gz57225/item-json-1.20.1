package io.itemjson.client;

import io.itemjson.client.keybind.KeyBind;
import net.fabricmc.api.ClientModInitializer;

public class ItemJsonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBind.register();
    }
}
