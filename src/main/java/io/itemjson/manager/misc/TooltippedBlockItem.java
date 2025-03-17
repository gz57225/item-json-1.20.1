package io.itemjson.manager.misc;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TooltippedBlockItem extends BlockItem {
    private final ArrayList<Text> TOOLTIPS;

    public TooltippedBlockItem(Block block, Settings settings, ArrayList<Text> TOOLTIPS) {
        super(block, settings);
        this.TOOLTIPS = TOOLTIPS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.addAll(TOOLTIPS);
    }
}
