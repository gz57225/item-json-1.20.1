package io.itemjson.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.itemjson.json.JsonReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static io.itemjson.ItemJson.MOD_ID;

public abstract class AbstractManager {
    public final JsonReader READER;
    private final String MOD_ID;

    public AbstractManager(String mod_id, String category, ClassLoader loader) {
        this.MOD_ID = mod_id;
        this.READER = JsonReader.of(mod_id, category, loader);
    }

    static @NotNull String getMethodName(String path) {
        return path.substring(path.indexOf('$') + 1);
    }

    static @NotNull String getClassName(String path) {
        return path.substring(0, path.indexOf('$'));
    }

    public String getModId() { return MOD_ID; }

    public <T> T getThis(Registry<T> registry, String name) {
        return registry.get(new Identifier(getModId(), name));
    }

    public static <T> T get(Registry<T> registry, String name) {
        return registry.get(new Identifier(name));
    }

    public abstract void registerAll() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    protected static <V> Object newClassByPath(String path, Class<V> settingsClass, V settings) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName(path);
        var constructor = clazz.getDeclaredConstructor(settingsClass);
        return constructor.newInstance(settings);
    }

    protected static Object newClassByPath(String path) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName(path);
        return clazz.getDeclaredConstructor().newInstance();
    }

    protected static Method methodByPath(String path, String methodName, Class<?>... paraClazz) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName(path);
        return clazz.getDeclaredMethod(methodName, paraClazz);
    }
}
