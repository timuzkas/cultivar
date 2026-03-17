package com.timuzkas.cultivar;

import java.util.Random;

public class StrainProfile {
    public String name;
    public float yieldBonus;
    public float speedMultiplier;
    public int stressResistance;
    public String potency;

    public StrainProfile(String name, float yieldBonus, float speedMultiplier, int stressResistance, String potency) {
        this.name = name;
        this.yieldBonus = yieldBonus;
        this.speedMultiplier = speedMultiplier;
        this.stressResistance = stressResistance;
        this.potency = potency;
    }

    private static final String[] PREFIXES = {
        "Highland", "Riverside", "Mountain", "Valley", "Forest", "Coastal",
        "Northern", "Southern", "Eastern", "Western", "Golden", "Silver",
        "Crystal", "Emerald", "Ruby", "Sapphire", "Amber", "Frost"
    };

    private static final String[] SUFFIXES = {
        "#1", "#2", "#3", "#4", "#5", "Gold", "Silver", "Supreme",
        "Classic", "Special", "Premium", "Select", "Elite", "Prime"
    };

    public static StrainProfile generate(String cropId) {
        int hash = cropId.hashCode();
        Random random = new Random(hash);

        String name = PREFIXES[random.nextInt(PREFIXES.length)] + " " +
                     SUFFIXES[random.nextInt(SUFFIXES.length)];

        float yieldBonus = 0.0f;
        float speedMultiplier = 1.0f;
        int stressResistance = 0;
        String potency;

        int roll = random.nextInt(100);
        if (roll < 30) {
            yieldBonus = 0.2f + random.nextFloat() * 0.3f;
            potency = "Balanced";
        } else if (roll < 50) {
            speedMultiplier = 0.8f + random.nextFloat() * 0.15f;
            potency = "Fast";
        } else if (roll < 65) {
            stressResistance = 1 + random.nextInt(2);
            potency = "Resilient";
        } else if (roll < 80) {
            yieldBonus = 0.4f + random.nextFloat() * 0.4f;
            potency = "High Yield";
        } else if (roll < 90) {
            stressResistance = 2;
            speedMultiplier = 0.85f;
            potency = "Premium";
        } else {
            yieldBonus = 0.5f;
            speedMultiplier = 0.75f;
            stressResistance = 1;
            potency = "Elite";
        }

        return new StrainProfile(name, yieldBonus, speedMultiplier, stressResistance, potency);
    }

    public String getId() {
        return name.replace(" ", "_").replace("#", "");
    }
}
