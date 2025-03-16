package io.itemjson.manager.misc;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TooltippedItem extends Item {
    public final ArrayList<Text> TOOLTIPS;
    public TooltippedItem(Settings settings, ArrayList<Text> TOOLTIPS) {
        super(settings);
        this.TOOLTIPS = TOOLTIPS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.addAll(TOOLTIPS);
    }
}
