package com.timuzkas.cultivar;

public enum PipeTier {
    CLAY(6, "§7", 1.0),
    WOOD(10, "§8", 1.0),
    MEERSCHAUM(16, "§f", 1.1);

    private final int maxUses;
    private final String color;
    private final double effectMultiplier;

    PipeTier(int maxUses, String color, double effectMultiplier) {
        this.maxUses = maxUses;
        this.color = color;
        this.effectMultiplier = effectMultiplier;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public String getColor() {
        return color;
    }

    public double getEffectMultiplier() {
        return effectMultiplier;
    }
}
