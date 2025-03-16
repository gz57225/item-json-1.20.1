package io.itemjson.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

public class BlockManager extends AbstractManager{
    public static final ArrayList<Block> BLOCKS = new ArrayList<>();

    private static final HashMap<String, BiConsumer<AbstractBlock.Settings, JsonElement>> ELEMENT_TO_FUNC_MAP = new HashMap<>();

    static {
        ELEMENT_TO_FUNC_MAP.put("name", (settings, element) -> {});
        ELEMENT_TO_FUNC_MAP.put("air", (settings, element) -> {
            if (element.getAsBoolean()) settings.air();
        });
        ELEMENT_TO_FUNC_MAP.put("allows_spawning", (settings, element) -> {
            switch (element.getAsString()) {
                case "always": settings.allowsSpawning(Blocks::always); break;
                case "never": settings.allowsSpawning(Blocks::never); break;
                default:
                    try {
                        settings.allowsSpawning(typedContextPredicate(element.getAsString()));
                    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                             InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
            }
        });
        ELEMENT_TO_FUNC_MAP.put("break_instantly", (settings, element) -> {
            if (element.getAsBoolean()) settings.breakInstantly();
        });
        ELEMENT_TO_FUNC_MAP.put("burnable", (settings, element) -> {
            if (element.getAsBoolean()) settings.burnable();
        });
        ELEMENT_TO_FUNC_MAP.put("drop_nothing", (settings, element) -> {
            if (element.getAsBoolean()) settings.dropsNothing();
        });
        ELEMENT_TO_FUNC_MAP.put("drops_like", (settings, element) -> {
            settings.dropsLike(Registries.BLOCK.get(new Identifier(element.getAsString())));
        });
        ELEMENT_TO_FUNC_MAP.put("emissiveLighting", (settings, element) -> {});
        ELEMENT_TO_FUNC_MAP.put("hardness", (settings, element) -> {
            settings.hardness(element.getAsFloat());
        });
        ELEMENT_TO_FUNC_MAP.put("jump_velocity_multiplier", (settings, element) -> {
            settings.jumpVelocityMultiplier(element.getAsFloat());
        });
        ELEMENT_TO_FUNC_MAP.put("liquid", (settings, element) -> {
            if (element.getAsBoolean()) settings.liquid();
        });
        ELEMENT_TO_FUNC_MAP.put("luminance", (settings, element) -> {
            if (element.getAsJsonPrimitive().isNumber()) {
                settings.luminance(state -> element.getAsInt());
            } else {
                String methodPath = element.getAsString();
                try {
                    settings.luminance(luminanceMethod(
                            methodPath
                    ));
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                         InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        ELEMENT_TO_FUNC_MAP.put("no_block_break_particles", (settings, element) -> {
            if (element.getAsBoolean()) settings.noBlockBreakParticles();
        });
        ELEMENT_TO_FUNC_MAP.put("no_collision", (settings, element) -> {
            if (element.getAsBoolean()) settings.noCollision();
        });
        ELEMENT_TO_FUNC_MAP.put("non_opaque", (settings, element) -> {
            if (element.getAsBoolean()) settings.nonOpaque();
        });
        ELEMENT_TO_FUNC_MAP.put("offset", (settings, element) -> {
            settings.offset(switch (element.getAsString()) {
                case "xyz": yield AbstractBlock.OffsetType.XYZ;
                case "xz": yield AbstractBlock.OffsetType.XZ;
                default: throw new IllegalStateException("Unexpected value: " + element.getAsString());
            });
        });
        ELEMENT_TO_FUNC_MAP.put("piston_behavior", (settings, element) -> {
            settings.pistonBehavior(switch (element.getAsString()) {
                case "normal":  yield PistonBehavior.NORMAL;
                case "destroy": yield PistonBehavior.DESTROY;
                case "block":   yield PistonBehavior.BLOCK;
                case "ignore":  yield PistonBehavior.IGNORE;
                case "push_only": yield PistonBehavior.PUSH_ONLY;
                default: throw new IllegalStateException("Unexpected value: " + element.getAsString());
            });
        });
        ELEMENT_TO_FUNC_MAP.put("replaceable", (settings, element) -> {
            if (element.getAsBoolean()) settings.replaceable();
        });
        ELEMENT_TO_FUNC_MAP.put("resistance", (settings, element) -> {
            settings.resistance(element.getAsFloat());
        });
        ELEMENT_TO_FUNC_MAP.put("requires_tool", (settings, element) -> {
            if (element.getAsBoolean()) settings.requiresTool();
        });
        ELEMENT_TO_FUNC_MAP.put("slipperiness", (settings, element) -> {
            settings.slipperiness(element.getAsFloat());
        });
        ELEMENT_TO_FUNC_MAP.put("strength", (settings, element) -> {
            settings.strength(element.getAsFloat());
        });
        ELEMENT_TO_FUNC_MAP.put("solid", (settings, element) -> {
            if (element.getAsBoolean()) settings.solid();
        });
        ELEMENT_TO_FUNC_MAP.put("velocity_multiplier", (settings, element) -> {
            settings.velocityMultiplier(element.getAsFloat());
        });

    }

    public BlockManager(String mod_id) {
        super(mod_id, "blocks");
    }

    public ArrayList<Block> registerAll() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ArrayList<JsonObject> jsonObjects = READER.listJsons();
        ArrayList<Block> blocks = new ArrayList<>();
        for (var jsonObject : jsonObjects) {
            Pair<Block, String> info = parse(jsonObject);
            blocks.add(Registry.register(
                    Registries.BLOCK,
                    RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(getModId(), info.getRight())),
                    info.getLeft()));
        }
        BLOCKS.addAll(blocks);
        return blocks;
    }

    protected Pair<Block, String> parse(JsonObject jsonObject) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String name = jsonObject.get("name").getAsString();
        AbstractBlock.Settings settings = AbstractBlock.Settings.create();

        for (Map.Entry<String, JsonElement> entry : jsonObject.asMap().entrySet()) {
            ELEMENT_TO_FUNC_MAP.get(entry.getKey()).accept(settings, entry.getValue());
        }

        if (jsonObject.has("class")) {
            String clazz = jsonObject.get("class").getAsString();
            return new Pair<>(newClassByPath(clazz, settings), name);
        }
        return new Pair<>(new Block(settings), name);
    }

    protected static Block newClassByPath(String path, Block.Settings settings) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return (Block) newClassByPath(path, Block.Settings.class, settings);
    }

    protected static ToIntFunction<BlockState> luminanceMethod(String path) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Method method = methodByPath(getClassName(path), getMethodName(path), BlockState.class);
        return state -> {
            try {
                return (int) method.invoke(null, state); // Pass `null` for static methods
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    protected static <V> AbstractBlock.TypedContextPredicate<EntityType<?>> typedContextPredicate(String path) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Method method = methodByPath(getClassName(path), getMethodName(path), BlockState.class, BlockView.class, BlockPos.class, EntityType.class);
        return (state, view, pos, entityType) -> {
            try {
                return (boolean) method.invoke(null, state, view, pos, entityType);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
