package io.itemjson.client.datagen;

import io.itemjson.ItemJson;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;

public class ItemJsonModelGenerator extends FabricModelProvider {
    public ItemJsonModelGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator gen) {
        ModelGenerator.generateBlockStateModels(gen, ItemJson.class.getClassLoader(), ItemJson.BLOCK_MANAGER);
    }

    @Override
    public void generateItemModels(ItemModelGenerator gen) {
        ModelGenerator.generateItemModels(gen, ItemJson.class.getClassLoader(), ItemJson.ITEM_MANAGER);
    }
}
