package io.itemjson.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mojang.text2speech.Narrator.LOGGER;

public class JsonReader {
    public static final Gson GSON = new Gson();
    private final String MOD_ID;
    private final String CATEGORY;
    private final Path PATH;

    private JsonReader(String mod_id, String category) {
        this.MOD_ID = mod_id;
        this.CATEGORY = category;
        this.PATH = Path.of("./itemjson/" + MOD_ID + "/" + CATEGORY);
    }

    public static JsonReader of(String mod_id, String category) {
        return new JsonReader(mod_id, category);
    }

    public ArrayList<JsonObject> listJsons() throws IOException {
        if (!Files.isDirectory(PATH)) {
            LOGGER.error("Provide Path {} Does Not Exist", PATH);
            return new ArrayList<>();
        }

        List<Path> JsonFiles = Files.walk(PATH, 1).filter(path -> path.toString().endsWith(".json")).toList();
        var results = new ArrayList<JsonObject>();
        results.ensureCapacity(JsonFiles.size());
        for (Path path : JsonFiles) {
            JsonObject element = GSON.fromJson(Files.newBufferedReader(path), JsonObject.class);
            results.add(element);
        }

        return results;
    }

    public String getModId() { return MOD_ID; }
}