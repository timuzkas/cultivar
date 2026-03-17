package com.timuzkas.cultivar;

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

    public static ItemStack createCannabisSeed() {
        ItemStack item = new ItemStack(Material.WHEAT_SEEDS, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§2Cannabis Seed");
        meta
            .getPersistentDataContainer()
            .set(CANNABIS_SEED, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createTobaccoSeed() {
        ItemStack item = new ItemStack(Material.WHEAT_SEEDS, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Tobacco Seed");
        meta
            .getPersistentDataContainer()
            .set(TOBACCO_SEED, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createTeaSeed() {
        ItemStack item = new ItemStack(Material.WHEAT_SEEDS, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bTea Seed");
        meta
            .getPersistentDataContainer()
            .set(TEA_SEED, PersistentDataType.BOOLEAN, true);
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
        ItemStack item = new ItemStack(Material.STICK, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§8⌐ Pipe");
        meta
            .getPersistentDataContainer()
            .set(PIPE_BLANK, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createFilledPipe(CropType material) {
        ItemStack item = new ItemStack(Material.STICK, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(
            material == CropType.CANNABIS
                ? "§2⌐ Pipe §8[Cannabis]"
                : "§6⌐ Pipe §8[Tobacco]"
        );
        meta
            .getPersistentDataContainer()
            .set(PIPE_FILLED, PersistentDataType.BOOLEAN, true);
        meta
            .getPersistentDataContainer()
            .set(PIPE_MATERIAL, PersistentDataType.STRING, material.name());
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createLitPipe(CropType material, int uses) {
        ItemStack item = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e⌐ Pipe §7(lit)");
        meta
            .getPersistentDataContainer()
            .set(PIPE_LIT, PersistentDataType.BOOLEAN, true);
        meta
            .getPersistentDataContainer()
            .set(PIPE_MATERIAL, PersistentDataType.STRING, material.name());
        meta
            .getPersistentDataContainer()
            .set(PIPE_USES, PersistentDataType.INTEGER, uses);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createCupOfTea(String variant) {
        ItemStack item = new ItemStack(Material.POTION, 1);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName("§bCup of Tea §8[" + variant + "]");

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
        ItemStack item = new ItemStack(Material.FLOWER_POT, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bTeapot §8[" + cups + "/4]");
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
        item.setItemMeta(meta);
        return item;
    }

    // Check methods
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

    // Getters
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
}
