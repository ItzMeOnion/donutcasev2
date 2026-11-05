package com.donutcase;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * One item inside a case. The worth is a rough, manually entered estimate in dollars.
 * The itemId is a vanilla item path, e.g. "dirt" or "netherite_ingot".
 */
public record CaseItem(String itemId, long worth) {

    /**
     * Rarity weight. Higher worth means a smaller weight, so a rarer drop.
     * 1/sqrt(worth) keeps items worth 100m+ well under 1% each.
     */
    public double weight() {
        return 1.0 / Math.sqrt((double) worth);
    }

    public ItemStack stack() {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath("minecraft", itemId)));
    }

    /** 1000 becomes "1k", 2_500_000 becomes "2.5m", 1_000_000_000_000 becomes "1T". */
    public static String formatWorth(long value) {
        if (value >= 1_000_000_000_000L) return trim(value / 1e12) + "T";
        if (value >= 1_000_000_000L) return trim(value / 1e9) + "b";
        if (value >= 1_000_000L) return trim(value / 1e6) + "m";
        if (value >= 1_000L) return trim(value / 1e3) + "k";
        return Long.toString(value);
    }

    private static String trim(double d) {
        return String.format(Locale.ROOT, "%.2f", d).replaceAll("\\.?0+$", "");
    }
}
