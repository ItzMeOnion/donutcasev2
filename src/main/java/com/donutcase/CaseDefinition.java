package com.donutcase;

import java.util.List;
import java.util.Random;

/** A case: a name, a colour and a weighted list of items. */
public final class CaseDefinition {

    public static final CaseDefinition DEFAULT = new CaseDefinition(
            "Starter Case",
            0xFF4FC3F7,
            List.of(
                    new CaseItem("dirt", 1_000L),
                    new CaseItem("leather_helmet", 2_000L),
                    new CaseItem("iron_ingot", 5_000L),
                    new CaseItem("diamond", 20_000L),
                    new CaseItem("diamond_sword", 22_000L),
                    new CaseItem("diamond_block", 100_000L),
                    new CaseItem("sponge", 2_500_000L),
                    new CaseItem("netherite_hoe", 3_000_000L),
                    new CaseItem("netherite_chestplate", 15_000_000L),
                    new CaseItem("netherite_ingot", 20_000_000L),
                    new CaseItem("dragon_head", 185_000_000L),
                    new CaseItem("netherite_block", 200_000_000L),
                    new CaseItem("elytra", 650_000_000L),
                    new CaseItem("bedrock", 1_000_000_000L),
                    new CaseItem("dragon_egg", 1_000_000_000_000L)
            ));

    private final String name;
    private final int colour;
    private final List<CaseItem> items;
    private final double totalWeight;

    public CaseDefinition(String name, int colour, List<CaseItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("A case needs at least one item");
        }
        this.name = name;
        this.colour = colour;
        this.items = List.copyOf(items);
        double total = 0;
        for (CaseItem item : this.items) {
            total += item.weight();
        }
        this.totalWeight = total;
    }

    public String name() {
        return name;
    }

    public int colour() {
        return colour;
    }

    public List<CaseItem> items() {
        return items;
    }

    /** Picks one item at random, weighted by rarity. */
    public CaseItem roll(Random random) {
        double r = random.nextDouble() * totalWeight;
        for (CaseItem item : items) {
            r -= item.weight();
            if (r <= 0) {
                return item;
            }
        }
        return items.get(items.size() - 1);
    }

    /** The chance (0 to 1) of getting this item from one opening. */
    public double chance(CaseItem item) {
        return item.weight() / totalWeight;
    }
}
