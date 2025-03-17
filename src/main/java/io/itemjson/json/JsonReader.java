package io.itemjson.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.itemjson.ItemJson;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static io.itemjson.ItemJson.THE_LOGGER;

public class JsonReader {
    public static final Gson GSON = new Gson();
    public final ClassLoader CLASS_LOADER;
    private final String PATH;

    private JsonReader(String mod_id, String category, ClassLoader loader) {
        this.PATH = "common/" + mod_id + "/" + category;
        this.CLASS_LOADER = loader;
    }

    public static JsonReader of(String mod_id, String category, ClassLoader loader) {
        return new JsonReader(mod_id, category, loader);
    }

    public ArrayList<JsonObject> listJsons() throws IOException {
        ArrayList<JsonObject> results = new ArrayList<>();

        Enumeration<URL> resources = CLASS_LOADER.getResources(PATH);
        while (resources.hasMoreElements()) {
            java.net.URL resource = resources.nextElement();
            try (InputStream inputStream = resource.openStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null && line.endsWith(".json")) {
                    results.add(getJson(line));
                }
            }
        }

        return results;
    }

    public JsonObject getJson(String name) {
        try (InputStreamReader reader = new InputStreamReader(
                CLASS_LOADER.getResourceAsStream(PATH + "/" + name))) {
            return GSON.fromJson(reader, JsonObject.class);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException(e);
        }
    }
}