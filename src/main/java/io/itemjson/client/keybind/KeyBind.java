package io.itemjson.client.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import static io.itemjson.ItemJson.LOGGER;


public class KeyBind {
    private static final KeyBinding TEST_KEY;

    static {
        LOGGER.info("KeyBind Registering");
        TEST_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.itemjson.test",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_BACKSLASH,
                "key.itemjson.category"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TEST_KEY.wasPressed()) {
                client.player.sendMessage(Text.literal("Testing Now!"), false);
                client.player.sendMessage(Text.literal("Finish Testing"), false);
            }
        });
    }

    public static void register() {}
}
