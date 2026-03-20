package com.timuzkas.cultivar;

import java.util.Random;

public class StrainProfile {
    public String name;
    public String strainId;
    public CropType cropType;
    
    // Cannabis stats
    public float yieldBonus;
    public float speedMultiplier;
    public int stressResistance;
    public String potency;
    
    // Tobacco stats
    public float curabilityBonus;
    public int leafYieldBonus;
    public String aromaProfile;
    
    // Tea stats
    public float brewStrength;
    public String rarityTag;
    
    // Mushroom stats
    public int potencyLevel;
    public int lightTolerance;
    public int sporeDensity;

    public StrainProfile(String strainId, String name, CropType cropType) {
        this.strainId = strainId;
        this.name = name;
        this.cropType = cropType;
    }

    public StrainProfile(String strainId, String name, CropType cropType, 
                        float yieldBonus, float speedMultiplier, int stressResistance, String potency) {
        this(strainId, name, cropType);
        this.yieldBonus = yieldBonus;
        this.speedMultiplier = speedMultiplier;
        this.stressResistance = stressResistance;
        this.potency = potency;
    }

    public StrainProfile(String strainId, String name, CropType cropType,
                        float curabilityBonus, int leafYieldBonus, String aromaProfile) {
        this(strainId, name, cropType);
        this.curabilityBonus = curabilityBonus;
        this.leafYieldBonus = leafYieldBonus;
        this.aromaProfile = aromaProfile;
    }

    public StrainProfile(String strainId, String name, CropType cropType,
                        float brewStrength, String rarityTag) {
        this(strainId, name, cropType);
        this.brewStrength = brewStrength;
        this.rarityTag = rarityTag;
    }

    public StrainProfile(String strainId, String name, CropType cropType,
                        int potencyLevel, int lightTolerance, int sporeDensity) {
        this(strainId, name, cropType);
        this.potencyLevel = potencyLevel;
        this.lightTolerance = lightTolerance;
        this.sporeDensity = sporeDensity;
    }

    private static final String[] CANNABIS_PREFIXES = {
        "Highland", "Riverside", "Mountain", "Valley", "Forest", "Coastal",
        "Northern", "Southern", "Eastern", "Western", "Golden", "Silver",
        "Crystal", "Emerald", "Ruby", "Sapphire", "Amber", "Frost"
    };

    private static final String[] CANNABIS_SUFFIXES = {
        "#1", "#2", "#3", "#4", "#5", "Gold", "Silver", "Supreme",
        "Classic", "Special", "Premium", "Select", "Elite", "Prime"
    };

    private static final String[] TOBACCO_PREFIXES = {
        "Virginia", "Burley", "Oriental", "Latakia", "Perique",
        "Maryland", "Havana", "Turkish", "Cavendish", "Kentucky",
        "Shire", "Valley", "River", "Golden", "Amber"
    };

    private static final String[] TOBACCO_SUFFIXES = {
        "Bright", "Dark", "Fire", "Sweet", "Mild", "Robust",
        "Leaf", "Crown", "Blend", "Choice", "Fine", "Rich"
    };

    private static final String[] TEA_PREFIXES = {
        "Darjeeling", "Assam", "Yunnan", "Ceylon", "Keemun",
        "Gyokuro", "Sencha", "Oolong", "Nilgiri", "Formosa",
        "Jasmine", "Earl", "Matcha", "White", "Green"
    };

    private static final String[] TEA_SUFFIXES = {
        "Mist", "Peak", "Valley", "Garden", "Leaf", "Bud",
        "Tip", "Blend", "Harvest", "Morning", "Evening", "Classic"
    };

    private static final String[] MUSHROOM_PREFIXES = {
        "Speckled", "Velvet", "Pale", "Deep", "Ancient",
        "Gnarled", "Luminous", "Shadow", "Forest", "Stone",
        "Crypt", "Cave", "Night", "Dawn", "Twilight"
    };

    private static final String[] MUSHROOM_SUFFIXES = {
        "Cap", "Stem", "Shroom", "Fungus", "Bloom",
        "Patch", "Cluster", "Crown", "Ring", "Veil", "Spore"
    };

    public static StrainProfile generate(String seed) {
        return generate(seed, CropType.CANNABIS);
    }

    public static StrainProfile generate(String seed, CropType type) {
        int hash = seed.hashCode();
        Random random = new Random(hash);
        
        switch (type) {
            case CANNABIS:
                return generateCannabis(seed, random);
            case TOBACCO:
                return generateTobacco(seed, random);
            case TEA:
                return generateTea(seed, random);
            case MUSHROOM:
                return generateMushroom(seed, random);
            default:
                return generateCannabis(seed, random);
        }
    }

    private static StrainProfile generateCannabis(String seed, Random random) {
        String name = CANNABIS_PREFIXES[random.nextInt(CANNABIS_PREFIXES.length)] + " " +
                     CANNABIS_SUFFIXES[random.nextInt(CANNABIS_SUFFIXES.length)];

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

        return new StrainProfile(seed, name, CropType.CANNABIS, yieldBonus, speedMultiplier, stressResistance, potency);
    }

    private static StrainProfile generateTobacco(String seed, Random random) {
        String name = TOBACCO_PREFIXES[random.nextInt(TOBACCO_PREFIXES.length)] + " " +
                     TOBACCO_SUFFIXES[random.nextInt(TOBACCO_SUFFIXES.length)];

        float curabilityBonus = 0.1f + random.nextFloat() * 0.3f;
        int leafYieldBonus = 1 + random.nextInt(3);
        
        String[] aromas = {"mild", "robust", "sweet", "smoky", "earthy"};
        String aromaProfile = aromas[random.nextInt(aromas.length)];

        return new StrainProfile(seed, name, CropType.TOBACCO, curabilityBonus, leafYieldBonus, aromaProfile);
    }

    private static StrainProfile generateTea(String seed, Random random) {
        String name = TEA_PREFIXES[random.nextInt(TEA_PREFIXES.length)] + " " +
                     TEA_SUFFIXES[random.nextInt(TEA_SUFFIXES.length)];

        float brewStrength = 0.1f + random.nextFloat() * 0.4f;
        
        String[] rarities = {"common", "common", "common", "rare", "rare", "aged"};
        String rarityTag = rarities[random.nextInt(rarities.length)];

        return new StrainProfile(seed, name, CropType.TEA, brewStrength, rarityTag);
    }

    private static StrainProfile generateMushroom(String seed, Random random) {
        String name = MUSHROOM_PREFIXES[random.nextInt(MUSHROOM_PREFIXES.length)] + " " +
                     MUSHROOM_SUFFIXES[random.nextInt(MUSHROOM_SUFFIXES.length)];

        int potencyLevel = 1 + random.nextInt(3);
        int lightTolerance = 3 + random.nextInt(5);
        int sporeDensity = 1 + random.nextInt(3);

        return new StrainProfile(seed, name, CropType.MUSHROOM, potencyLevel, lightTolerance, sporeDensity);
    }
}
