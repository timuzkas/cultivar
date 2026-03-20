package com.timuzkas.cultivar;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

@SuppressWarnings("deprecation")
public class ItemFactory {

    private static final NamespacedKey CANNABIS_SEED = new NamespacedKey(
        "cultivar",
        "cannabis_seed"
    );
    private static final NamespacedKey TOBACCO_SEED = new NamespacedKey(
        "cultivar",
        "tobacco_seed"
    );
    private static final NamespacedKey TEA_SEED = new NamespacedKey(
        "cultivar",
        "tea_seed"
    );
    private static final NamespacedKey CANNABIS_BUD = new NamespacedKey(
        "cultivar",
        "cannabis_bud"
    );
    private static final NamespacedKey WET_LEAF = new NamespacedKey(
        "cultivar",
        "wet_leaf"
    );
    private static final NamespacedKey DRY_LEAF = new NamespacedKey(
        "cultivar",
        "dry_leaf"
    );
    private static final NamespacedKey TEA_LEAF = new NamespacedKey(
        "cultivar",
        "tea_leaf"
    );
    private static final NamespacedKey COMPOST = new NamespacedKey(
        "cultivar",
        "compost"
    );
    private static final NamespacedKey PIPE_BLANK = new NamespacedKey(
        "cultivar",
        "pipe_blank"
    );
    private static final NamespacedKey PIPE_FILLED = new NamespacedKey(
        "cultivar",
        "pipe_filled"
    );
    private static final NamespacedKey PIPE_MATERIAL = new NamespacedKey(
        "cultivar",
        "pipe_material"
    );
    private static final NamespacedKey PIPE_LIT = new NamespacedKey(
        "cultivar",
        "pipe_lit"
    );
    private static final NamespacedKey PIPE_USES = new NamespacedKey(
        "cultivar",
        "pipe_uses"
    );
    private static final NamespacedKey TEA_CUP = new NamespacedKey(
        "cultivar",
        "tea_cup"
    );
    private static final NamespacedKey TEA_VARIANT = new NamespacedKey(
        "cultivar",
        "tea_variant"
    );
    private static final NamespacedKey TEAPOT_EMPTY = new NamespacedKey(
        "cultivar",
        "teapot_empty"
    );
    private static final NamespacedKey TEAPOT_BREWED = new NamespacedKey(
        "cultivar",
        "teapot_brewed"
    );
    private static final NamespacedKey TEA_CUPS = new NamespacedKey(
        "cultivar",
        "tea_cups"
    );
    private static final NamespacedKey DRIED_TEA_LEAF = new NamespacedKey(
        "cultivar",
        "dried_tea_leaf"
    );
    private static final NamespacedKey STRAIN_ID = new NamespacedKey(
        "cultivar",
        "strain_id"
    );
    private static final NamespacedKey STRAIN_NAME = new NamespacedKey(
        "cultivar",
        "strain_name"
    );
    private static final NamespacedKey CANNABIS_TRIM = new NamespacedKey(
        "cultivar",
        "cannabis_trim"
    );
    private static final NamespacedKey ORIGINAL_BREEDER = new NamespacedKey(
        "cultivar",
        "original_breeder"
    );
    private static final NamespacedKey ORIGINAL_BREEDER_NAME = new NamespacedKey(
        "cultivar",
        "original_breeder_name"
    );
    private static final NamespacedKey TEA_QUALITY = new NamespacedKey(
        "cultivar",
        "tea_quality"
    );
    private static final NamespacedKey TEA_BLEND = new NamespacedKey(
        "cultivar",
        "tea_blend"
    );
    private static final NamespacedKey PIPE_TIER = new NamespacedKey(
        "cultivar",
        "pipe_tier"
    );
    private static final NamespacedKey PIPE_SMOKES = new NamespacedKey(
        "cultivar",
        "pipe_smokes"
    );
    private static final NamespacedKey PIPE_SEASONED = new NamespacedKey(
        "cultivar",
        "pipe_seasoned"
    );
    private static final NamespacedKey SPORE = new NamespacedKey(
        "cultivar",
        "spore"
    );
    private static final NamespacedKey DRIED_MUSHROOM = new NamespacedKey(
        "cultivar",
        "dried_mushroom"
    );
    private static final NamespacedKey MUSHROOM_SEED = new NamespacedKey(
        "cultivar",
        "mushroom_seed"
    );
    private static final NamespacedKey HARVEST_BASKET = new NamespacedKey(
        "cultivar",
        "harvest_basket"
    );
    private static final NamespacedKey AIR_CURED_LEAF = new NamespacedKey(
        "cultivar",
        "air_cured_leaf"
    );
    private static final NamespacedKey FERMENTED_BUD = new NamespacedKey(
        "cultivar",
        "fermented_bud"
    );
    private static final NamespacedKey AGED_TOBACCO = new NamespacedKey(
        "cultivar",
        "aged_tobacco"
    );
    private static final NamespacedKey RACK_DRIED_TEA = new NamespacedKey(
        "cultivar",
        "rack_dried_tea"
    );
    private static final NamespacedKey SPLIFF = new NamespacedKey(
        "cultivar",
        "spliff"
    );
    private static final NamespacedKey HERBAL_FILL = new NamespacedKey(
        "cultivar",
        "herbal_fill"
    );
    private static final NamespacedKey SPICED_TEA_LEAF = new NamespacedKey(
        "cultivar",
        "spiced_tea_leaf"
    );
    private static final NamespacedKey SEED_QUALITY = new NamespacedKey(
        "cultivar",
        "seed_quality"
    );

    public static ItemStack createCannabisSeed() {
        return createCannabisSeed(null, null, null, null, 0.0f);
    }

    public static ItemStack createCannabisSeed(
        String strainId,
        String strainName
    ) {
        return createCannabisSeed(strainId, strainName, null, null, 0.0f);
    }

    public static ItemStack createCannabisSeed(
        String strainId,
        String strainName,
        String breederUuid,
        String breederName
    ) {
        return createCannabisSeed(strainId, strainName, breederUuid, breederName, 0.0f);
    }

    public static ItemStack createCannabisSeed(
        String strainId,
        String strainName,
        String breederUuid,
        String breederName,
        float seedQuality
    ) {
        ItemStack item = new ItemStack(Material.WHEAT_SEEDS, 1);
        ItemMeta meta = item.getItemMeta();
        String displayName;
        List<String> lore = new ArrayList<>();
        
        if (strainName != null) {
            displayName = "§2Cannabis Seed §7[" + strainName + "]";
            if (breederName != null) {
                lore.add("§8Grown by §7" + breederName);
            }
            if (seedQuality > 0) {
                String qualityStr = seedQuality >= 0.10f ? "§6Master" : "§eQuality";
                lore.add("§8Seed: " + qualityStr);
            }
        } else {
            displayName = "§2Cannabis Seed";
        }
        
        meta.setDisplayName(displayName);
        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }
        meta
            .getPersistentDataContainer()
            .set(CANNABIS_SEED, PersistentDataType.BOOLEAN, true);
        if (strainId != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_ID, PersistentDataType.STRING, strainId);
        }
        if (strainName != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_NAME, PersistentDataType.STRING, strainName);
        }
        if (breederUuid != null) {
            meta
                .getPersistentDataContainer()
                .set(ORIGINAL_BREEDER, PersistentDataType.STRING, breederUuid);
        }
        if (breederName != null) {
            meta
                .getPersistentDataContainer()
                .set(ORIGINAL_BREEDER_NAME, PersistentDataType.STRING, breederName);
        }
        if (seedQuality > 0) {
            meta
                .getPersistentDataContainer()
                .set(SEED_QUALITY, PersistentDataType.FLOAT, seedQuality);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createTobaccoSeed() {
        return createTobaccoSeed(null, null, null, null);
    }

    public static ItemStack createTobaccoSeed(String strainId, String strainName) {
        return createTobaccoSeed(strainId, strainName, null, null);
    }

    public static ItemStack createTobaccoSeed(String strainId, String strainName, String breederUuid, String breederName) {
        ItemStack item = new ItemStack(Material.WHEAT_SEEDS, 1);
        ItemMeta meta = item.getItemMeta();
        String displayName = strainName != null 
            ? "§6Tobacco Seed §7[" + strainName + "]" 
            : "§6Tobacco Seed";
        List<String> lore = new ArrayList<>();
        if (strainName != null && breederName != null) {
            lore.add("§8Grown by §7" + breederName);
        }
        
        meta.setDisplayName(displayName);
        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }
        meta
            .getPersistentDataContainer()
            .set(TOBACCO_SEED, PersistentDataType.BOOLEAN, true);
        if (strainId != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_ID, PersistentDataType.STRING, strainId);
        }
        if (strainName != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_NAME, PersistentDataType.STRING, strainName);
        }
        if (breederUuid != null) {
            meta
                .getPersistentDataContainer()
                .set(ORIGINAL_BREEDER, PersistentDataType.STRING, breederUuid);
        }
        if (breederName != null) {
            meta
                .getPersistentDataContainer()
                .set(ORIGINAL_BREEDER_NAME, PersistentDataType.STRING, breederName);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createTeaSeed() {
        return createTeaSeed(null, null, null, null);
    }

    public static ItemStack createTeaSeed(String strainId, String strainName) {
        return createTeaSeed(strainId, strainName, null, null);
    }

    public static ItemStack createTeaSeed(String strainId, String strainName, String breederUuid, String breederName) {
        ItemStack item = new ItemStack(Material.WHEAT_SEEDS, 1);
        ItemMeta meta = item.getItemMeta();
        String displayName = strainName != null 
            ? "§bTea Seed §7[" + strainName + "]" 
            : "§bTea Seed";
        List<String> lore = new ArrayList<>();
        if (strainName != null && breederName != null) {
            lore.add("§8Grown by §7" + breederName);
        }
        
        meta.setDisplayName(displayName);
        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }
        meta
            .getPersistentDataContainer()
            .set(TEA_SEED, PersistentDataType.BOOLEAN, true);
        if (strainId != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_ID, PersistentDataType.STRING, strainId);
        }
        if (strainName != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_NAME, PersistentDataType.STRING, strainName);
        }
        if (breederUuid != null) {
            meta
                .getPersistentDataContainer()
                .set(ORIGINAL_BREEDER, PersistentDataType.STRING, breederUuid);
        }
        if (breederName != null) {
            meta
                .getPersistentDataContainer()
                .set(ORIGINAL_BREEDER_NAME, PersistentDataType.STRING, breederName);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createCannabisBud() {
        ItemStack item = new ItemStack(Material.LIME_DYE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§2Cannabis Bud");
        meta
            .getPersistentDataContainer()
            .set(CANNABIS_BUD, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createCannabisTrim() {
        return createCannabisTrim(null, null);
    }

    public static ItemStack createCannabisTrim(String strainId, String strainName) {
        ItemStack item = new ItemStack(Material.KELP, 1);
        ItemMeta meta = item.getItemMeta();
        String displayName = strainName != null
            ? "§2Cannabis Trim §7[" + strainName + "]"
            : "§2Cannabis Trim";
        meta.setDisplayName(displayName);
        meta
            .getPersistentDataContainer()
            .set(CANNABIS_TRIM, PersistentDataType.BOOLEAN, true);
        if (strainId != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_ID, PersistentDataType.STRING, strainId);
        }
        if (strainName != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_NAME, PersistentDataType.STRING, strainName);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createWetTobaccoLeaf() {
        ItemStack item = new ItemStack(Material.DEAD_BUSH, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Wet Tobacco Leaf");
        meta
            .getPersistentDataContainer()
            .set(WET_LEAF, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createDryTobaccoLeaf() {
        ItemStack item = new ItemStack(Material.DEAD_BUSH, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7Dry Tobacco Leaf");
        meta
            .getPersistentDataContainer()
            .set(DRY_LEAF, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createAirCuredLeaf() {
        ItemStack item = new ItemStack(Material.DEAD_BUSH, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§fAir-cured Leaf");
        meta.setLore(java.util.List.of("§7Slow-dried, smooth"));
        meta
            .getPersistentDataContainer()
            .set(AIR_CURED_LEAF, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isAirCuredLeaf(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(AIR_CURED_LEAF, PersistentDataType.BOOLEAN)
        );
    }

    public static ItemStack createFermentedBud() {
        ItemStack item = new ItemStack(Material.LIME_DYE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§2Fermented Bud");
        meta.setLore(java.util.List.of("§8Slow-cured in darkness"));
        meta
            .getPersistentDataContainer()
            .set(FERMENTED_BUD, PersistentDataType.BOOLEAN, true);
        meta
            .getPersistentDataContainer()
            .set(CANNABIS_BUD, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createAgedTobacco() {
        ItemStack item = new ItemStack(Material.DEAD_BUSH, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Aged Tobacco");
        meta.setLore(java.util.List.of("§8Time and darkness"));
        meta
            .getPersistentDataContainer()
            .set(AGED_TOBACCO, PersistentDataType.BOOLEAN, true);
        meta
            .getPersistentDataContainer()
            .set(DRY_LEAF, PersistentDataType.BOOLEAN, true);
        meta
            .getPersistentDataContainer()
            .set(new NamespacedKey("cultivar", "cure_type"), 
                PersistentDataType.STRING, "aged");
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isFermented(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        return item.getItemMeta().getPersistentDataContainer()
            .has(FERMENTED_BUD, PersistentDataType.BOOLEAN);
    }

    public static boolean isAgedTobacco(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        return item.getItemMeta().getPersistentDataContainer()
            .has(AGED_TOBACCO, PersistentDataType.BOOLEAN);
    }

    public static ItemStack createRackDriedTeaLeaf() {
        ItemStack item = new ItemStack(Material.DEAD_BUSH, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bRack-dried Tea Leaf");
        meta
            .getPersistentDataContainer()
            .set(RACK_DRIED_TEA, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isRackDriedTeaLeaf(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(RACK_DRIED_TEA, PersistentDataType.BOOLEAN)
        );
    }

    public static ItemStack createFreshTeaLeaf() {
        ItemStack item = new ItemStack(Material.LARGE_FERN, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bFresh Tea Leaf");
        meta
            .getPersistentDataContainer()
            .set(TEA_LEAF, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createCompost() {
        ItemStack item = new ItemStack(Material.BONE_MEAL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aCompost");
        meta
            .getPersistentDataContainer()
            .set(COMPOST, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createBlankPipe() {
        return createBlankPipe(PipeTier.WOOD);
    }

    public static ItemStack createBlankPipe(PipeTier tier) {
        ItemStack item = new ItemStack(Material.STICK, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(tier.getColor() + "⌐ " + tier.name() + " Pipe");
        meta
            .getPersistentDataContainer()
            .set(PIPE_BLANK, PersistentDataType.BOOLEAN, true);
        meta
            .getPersistentDataContainer()
            .set(PIPE_TIER, PersistentDataType.STRING, tier.name());
        meta
            .getPersistentDataContainer()
            .set(PIPE_SMOKES, PersistentDataType.INTEGER, 0);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createClayPipe() {
        return createBlankPipe(PipeTier.CLAY);
    }

    public static ItemStack createMeerschaumPipe() {
        return createBlankPipe(PipeTier.MEERSCHAUM);
    }

    public static ItemStack createFilledPipe(CropType material) {
        return createFilledPipe(material, PipeTier.WOOD);
    }

    public static ItemStack createFilledPipe(CropType material, PipeTier tier) {
        ItemStack item = new ItemStack(Material.STICK, 1);
        ItemMeta meta = item.getItemMeta();
        String materialStr =
            material == CropType.CANNABIS ? "Cannabis" : "Tobacco";
        meta.setDisplayName(
            tier.getColor() +
                "⌐ " +
                tier.name() +
                " Pipe §8[" +
                materialStr +
                "]"
        );
        meta
            .getPersistentDataContainer()
            .set(PIPE_FILLED, PersistentDataType.BOOLEAN, true);
        meta
            .getPersistentDataContainer()
            .set(PIPE_MATERIAL, PersistentDataType.STRING, material.name());
        meta
            .getPersistentDataContainer()
            .set(PIPE_TIER, PersistentDataType.STRING, tier.name());
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createLitPipe(CropType material, int uses) {
        return createLitPipe(material, uses, PipeTier.WOOD, 0);
    }

    public static ItemStack createLitPipe(
        CropType material,
        int uses,
        PipeTier tier,
        int smokesUsed
    ) {
        ItemStack item = new ItemStack(Material.STICK, 1);
        ItemMeta meta = item.getItemMeta();
        String seasonedStr = smokesUsed >= 3 ? " §o(seasoned)" : "";
        meta.setDisplayName(
            tier.getColor() + "⌐ " + tier.name() + " Pipe §7(lit)" + seasonedStr
        );
        meta
            .getPersistentDataContainer()
            .set(PIPE_LIT, PersistentDataType.BOOLEAN, true);
        meta
            .getPersistentDataContainer()
            .set(PIPE_MATERIAL, PersistentDataType.STRING, material.name());
        meta
            .getPersistentDataContainer()
            .set(PIPE_USES, PersistentDataType.INTEGER, uses);
        meta
            .getPersistentDataContainer()
            .set(PIPE_TIER, PersistentDataType.STRING, tier.name());
        meta
            .getPersistentDataContainer()
            .set(PIPE_SMOKES, PersistentDataType.INTEGER, smokesUsed);
        if (smokesUsed >= 3) {
            meta
                .getPersistentDataContainer()
                .set(PIPE_SEASONED, PersistentDataType.BOOLEAN, true);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createSpliff(String strainId, String strainName, String cureType) {
        ItemStack item = new ItemStack(Material.STICK, 1);
        ItemMeta meta = item.getItemMeta();
        String displayName = "§2Spliff";
        if (strainName != null) {
            displayName += " §7[" + strainName + "]";
        }
        if (cureType != null) {
            displayName += " §8[" + cureType + "]";
        }
        meta.setDisplayName(displayName);
        meta
            .getPersistentDataContainer()
            .set(SPLIFF, PersistentDataType.BOOLEAN, true);
        if (strainId != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_ID, PersistentDataType.STRING, strainId);
        }
        if (strainName != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_NAME, PersistentDataType.STRING, strainName);
        }
        if (cureType != null) {
            meta
                .getPersistentDataContainer()
                .set(new NamespacedKey("cultivar", "cure_type"), PersistentDataType.STRING, cureType);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isSpliff(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(SPLIFF, PersistentDataType.BOOLEAN)
        );
    }

    public static ItemStack createHerbalFill() {
        ItemStack item = new ItemStack(Material.STICK, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§dHerbal Fill");
        meta
            .getPersistentDataContainer()
            .set(HERBAL_FILL, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isHerbalFill(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(HERBAL_FILL, PersistentDataType.BOOLEAN)
        );
    }

    public static ItemStack createSpicedTeaLeaf(String strainId, String strainName) {
        ItemStack item = new ItemStack(Material.LARGE_FERN, 1);
        ItemMeta meta = item.getItemMeta();
        String displayName = "§6Spiced Tea Leaf";
        if (strainName != null) {
            displayName += " §7[" + strainName + "]";
        }
        meta.setDisplayName(displayName);
        meta
            .getPersistentDataContainer()
            .set(SPICED_TEA_LEAF, PersistentDataType.BOOLEAN, true);
        if (strainId != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_ID, PersistentDataType.STRING, strainId);
        }
        if (strainName != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_NAME, PersistentDataType.STRING, strainName);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isSpicedTeaLeaf(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(SPICED_TEA_LEAF, PersistentDataType.BOOLEAN)
        );
    }

    public static ItemStack createCupOfTea(String variant) {
        return createCupOfTea(variant, null, null);
    }

    public static ItemStack createCupOfTea(String variant, String strainId, String strainName) {
        ItemStack item = new ItemStack(Material.POTION, 1);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return item;

        String strainDisplay = strainName != null ? " §8[" + strainName + "]" : "";
        meta.setDisplayName("§bCup of Tea §8[" + variant + "]" + strainDisplay);

        try {
            try {
                java.lang.reflect.Method setBaseType = meta
                    .getClass()
                    .getMethod("setBasePotionType", PotionType.class);
                setBaseType.invoke(meta, PotionType.MUNDANE);
            } catch (NoSuchMethodException e) {
                meta.setBasePotionData(new PotionData(PotionType.MUNDANE));
            }
        } catch (Exception e) {}

        Color color = switch (variant.toLowerCase()) {
            case "green" -> Color.fromRGB(111, 175, 95);
            case "black" -> Color.fromRGB(75, 46, 31);
            case "herbal" -> Color.fromRGB(138, 107, 190);
            default -> Color.fromRGB(139, 90, 43);
        };
        meta.setColor(color);

        meta.addCustomEffect(
            new PotionEffect(
                PotionEffectType.LUCK,
                1200,
                0,
                false,
                false,
                true
            ),
            true
        );

        meta
            .getPersistentDataContainer()
            .set(TEA_CUP, PersistentDataType.BOOLEAN, true);
        meta
            .getPersistentDataContainer()
            .set(TEA_VARIANT, PersistentDataType.STRING, variant);
        if (strainId != null) {
            meta.getPersistentDataContainer().set(STRAIN_ID, PersistentDataType.STRING, strainId);
        }
        if (strainName != null) {
            meta.getPersistentDataContainer().set(STRAIN_NAME, PersistentDataType.STRING, strainName);
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createEmptyTeapot() {
        ItemStack item = new ItemStack(Material.FLOWER_POT, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bTeapot");
        meta.setLore(List.of("§7Click on tea cauldron to fill"));
        meta
            .getPersistentDataContainer()
            .set(TEAPOT_EMPTY, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createBrewedTeapot(String variant, int cups) {
        return createBrewedTeapot(variant, cups, null, null, null, null);
    }

    public static ItemStack createBrewedTeapot(
        String variant,
        int cups,
        String quality,
        String blend
    ) {
        return createBrewedTeapot(variant, cups, quality, blend, null, null);
    }

    public static ItemStack createBrewedTeapot(
        String variant,
        int cups,
        String quality,
        String blend,
        String strainId,
        String strainName
    ) {
        ItemStack item = new ItemStack(Material.FLOWER_POT, 1);
        ItemMeta meta = item.getItemMeta();
        String strainDisplay = strainName != null ? " §8[" + strainName + "]" : "";
        String qualityStr = quality != null ? " §8[" + quality + "]" : "";
        meta.setDisplayName("§bTeapot §8[" + cups + "/4]" + qualityStr + strainDisplay);
        meta.setLore(List.of("§7RMB to fill bottle"));
        meta
            .getPersistentDataContainer()
            .set(TEAPOT_BREWED, PersistentDataType.BOOLEAN, true);
        meta
            .getPersistentDataContainer()
            .set(TEA_VARIANT, PersistentDataType.STRING, variant);
        meta
            .getPersistentDataContainer()
            .set(TEA_CUPS, PersistentDataType.INTEGER, cups);
        if (quality != null) {
            meta
                .getPersistentDataContainer()
                .set(TEA_QUALITY, PersistentDataType.STRING, quality);
        }
        if (blend != null) {
            meta
                .getPersistentDataContainer()
                .set(TEA_BLEND, PersistentDataType.STRING, blend);
        }
        if (strainId != null) {
            meta.getPersistentDataContainer().set(STRAIN_ID, PersistentDataType.STRING, strainId);
        }
        if (strainName != null) {
            meta.getPersistentDataContainer().set(STRAIN_NAME, PersistentDataType.STRING, strainName);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isCannabisSeed(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(CANNABIS_SEED, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isTobaccoSeed(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(TOBACCO_SEED, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isTeaSeed(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(TEA_SEED, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isCannabisBud(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(CANNABIS_BUD, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isCannabisTrim(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(CANNABIS_TRIM, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isWetTobaccoLeaf(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(WET_LEAF, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isDryTobaccoLeaf(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(DRY_LEAF, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isFreshTeaLeaf(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(TEA_LEAF, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isCompost(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(COMPOST, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isBlankPipe(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(PIPE_BLANK, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isFilledPipe(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(PIPE_FILLED, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isLitPipe(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(PIPE_LIT, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isCupOfTea(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(TEA_CUP, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isEmptyTeapot(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(TEAPOT_EMPTY, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isBrewedTeapot(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(TEAPOT_BREWED, PersistentDataType.BOOLEAN)
        );
    }

    public static CropType getPipeMaterial(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return null;
        String material = item
            .getItemMeta()
            .getPersistentDataContainer()
            .get(PIPE_MATERIAL, PersistentDataType.STRING);
        return material != null ? CropType.valueOf(material) : null;
    }

    public static int getPipeUses(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return 0;
        Integer uses = item
            .getItemMeta()
            .getPersistentDataContainer()
            .get(PIPE_USES, PersistentDataType.INTEGER);
        return uses != null ? uses : 0;
    }

    public static String getTeaVariant(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return null;
        return item
            .getItemMeta()
            .getPersistentDataContainer()
            .get(TEA_VARIANT, PersistentDataType.STRING);
    }

    public static int getTeaCups(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return 0;
        Integer cups = item
            .getItemMeta()
            .getPersistentDataContainer()
            .get(TEA_CUPS, PersistentDataType.INTEGER);
        return cups != null ? cups : 0;
    }

    public static ItemStack createDriedTeaLeaf() {
        ItemStack item = new ItemStack(Material.DEAD_BUSH, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Dried Tea Leaf");
        meta
            .getPersistentDataContainer()
            .set(DRIED_TEA_LEAF, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isDriedTeaLeaf(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(DRIED_TEA_LEAF, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isAnyTeaLeaf(ItemStack item) {
        return isFreshTeaLeaf(item) || isDriedTeaLeaf(item);
    }

    public static String getStrainId(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return null;
        return item
            .getItemMeta()
            .getPersistentDataContainer()
            .get(STRAIN_ID, PersistentDataType.STRING);
    }

    public static String getStrainName(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return null;
        return item
            .getItemMeta()
            .getPersistentDataContainer()
            .get(STRAIN_NAME, PersistentDataType.STRING);
    }

    public static String getOriginalBreeder(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return null;
        return item
            .getItemMeta()
            .getPersistentDataContainer()
            .get(ORIGINAL_BREEDER, PersistentDataType.STRING);
    }

    public static String getOriginalBreederName(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                if (line.contains("Grown by")) {
                    return line.replace("§8Grown by §7", "").trim();
                }
            }
        }
        return item
            .getItemMeta()
            .getPersistentDataContainer()
            .get(ORIGINAL_BREEDER_NAME, PersistentDataType.STRING);
    }

    public static float getSeedQuality(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return 0.0f;
        Float quality = item
            .getItemMeta()
            .getPersistentDataContainer()
            .get(SEED_QUALITY, PersistentDataType.FLOAT);
        return quality != null ? quality : 0.0f;
    }

    public static String getTeaQuality(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return null;
        return item
            .getItemMeta()
            .getPersistentDataContainer()
            .get(TEA_QUALITY, PersistentDataType.STRING);
    }

    public static String getTeaBlend(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return null;
        return item
            .getItemMeta()
            .getPersistentDataContainer()
            .get(TEA_BLEND, PersistentDataType.STRING);
    }

    public static PipeTier getPipeTier(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return PipeTier.WOOD;
        String tier = item
            .getItemMeta()
            .getPersistentDataContainer()
            .get(PIPE_TIER, PersistentDataType.STRING);
        return tier != null ? PipeTier.valueOf(tier) : PipeTier.WOOD;
    }

    public static int getPipeSmokesUsed(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return 0;
        Integer smokes = item
            .getItemMeta()
            .getPersistentDataContainer()
            .get(PIPE_SMOKES, PersistentDataType.INTEGER);
        return smokes != null ? smokes : 0;
    }

    public static boolean isPipeSeasoned(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        return item
            .getItemMeta()
            .getPersistentDataContainer()
            .has(PIPE_SEASONED, PersistentDataType.BOOLEAN);
    }

    public static ItemStack createSporeItem() {
        ItemStack item = new ItemStack(Material.BROWN_MUSHROOM, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§dMushroom Spores");
        meta
            .getPersistentDataContainer()
            .set(SPORE, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createMushroomSeed() {
        return createMushroomSeed(null, null, null, null);
    }

    public static ItemStack createMushroomSeed(String strainId, String strainName) {
        return createMushroomSeed(strainId, strainName, null, null);
    }

    public static ItemStack createMushroomSeed(String strainId, String strainName, String breederUuid, String breederName) {
        ItemStack item = new ItemStack(Material.BROWN_MUSHROOM, 1);
        ItemMeta meta = item.getItemMeta();
        String displayName = strainName != null 
            ? "§dMushroom Spore §7[" + strainName + "]" 
            : "§dMushroom Spore";
        List<String> lore = new ArrayList<>();
        if (strainName != null && breederName != null) {
            lore.add("§8Grown by §7" + breederName);
        }
        
        meta.setDisplayName(displayName);
        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }
        meta
            .getPersistentDataContainer()
            .set(MUSHROOM_SEED, PersistentDataType.BOOLEAN, true);
        if (strainId != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_ID, PersistentDataType.STRING, strainId);
        }
        if (strainName != null) {
            meta
                .getPersistentDataContainer()
                .set(STRAIN_NAME, PersistentDataType.STRING, strainName);
        }
        if (breederUuid != null) {
            meta
                .getPersistentDataContainer()
                .set(ORIGINAL_BREEDER, PersistentDataType.STRING, breederUuid);
        }
        if (breederName != null) {
            meta
                .getPersistentDataContainer()
                .set(ORIGINAL_BREEDER_NAME, PersistentDataType.STRING, breederName);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createDriedMushroom() {
        ItemStack item = new ItemStack(Material.COCOA_BEANS, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§dDried Mushroom");
        meta
            .getPersistentDataContainer()
            .set(DRIED_MUSHROOM, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isSpore(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(SPORE, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isMushroomSeed(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(MUSHROOM_SEED, PersistentDataType.BOOLEAN)
        );
    }

    public static boolean isDriedMushroom(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(DRIED_MUSHROOM, PersistentDataType.BOOLEAN)
        );
    }

    public static ItemStack createHarvestBasket() {
        ItemStack item = new ItemStack(Material.RABBIT_HIDE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(
            java.util.List.of(
                "§7Right-click crop to store harvests",
                "§7Right-click air to dump contents"
            )
        );
        meta.setDisplayName("§6Harvest Basket");
        meta
            .getPersistentDataContainer()
            .set(HARVEST_BASKET, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isHarvestBasket(ItemStack item) {
        return (
            item != null &&
            item.getItemMeta() != null &&
            item
                .getItemMeta()
                .getPersistentDataContainer()
                .has(HARVEST_BASKET, PersistentDataType.BOOLEAN)
        );
    }
}
