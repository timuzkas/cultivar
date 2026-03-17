package com.timuzkas.cultivar;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import java.util.stream.Collectors;

public class CropInteractListener implements Listener {

    private final CropManager cropManager;
    private final ActionBarAnimator animator;
    private final org.bukkit.plugin.Plugin plugin;
    private final java.util.Map<java.util.UUID, org.bukkit.Location> pendingRemovals = new java.util.HashMap<>();
    private final java.util.Map<org.bukkit.Location, Integer> mushroomRightClicks = new java.util.HashMap<>();
    private SoilManager soilManager;
    private PlayerStrainManager strainManager;

    public CropInteractListener(
        CropManager cropManager,
        ActionBarAnimator animator,
        org.bukkit.plugin.Plugin plugin
    ) {
        this.cropManager = cropManager;
        this.animator = animator;
        this.plugin = plugin;
    }

    public void setSoilManager(SoilManager soilManager) {
        this.soilManager = soilManager;
    }

    public void setStrainManager(PlayerStrainManager strainManager) {
        this.strainManager = strainManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (
            event.getAction() != Action.RIGHT_CLICK_BLOCK &&
            event.getAction() != Action.LEFT_CLICK_BLOCK
        ) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;

        CropRecord crop = cropManager.getByLocation(block.getLocation());
        if (crop == null) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && crop.type == CropType.MUSHROOM) {
            int clickCount = mushroomRightClicks.getOrDefault(block.getLocation(), 0) + 1;
            mushroomRightClicks.put(block.getLocation(), clickCount);
            if (clickCount > 3) {
                crop.stress += 1;
                crop.flags.add(CropFlag.STUNTED);
                crop.dirty = true;
                animator.reveal(player, "§c⚠ Stop fussing — mushrooms hate it", null);
                mushroomRightClicks.remove(block.getLocation());
            }
        }

        ItemStack item = event.getItem();
        boolean sneaking = player.isSneaking();

        // Prevent vanilla pot interaction / removing the plant on click
        event.setCancelled(true);
        event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
        event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);

        // Removal (left-click twice with shears)
        if (
            event.getAction() == Action.LEFT_CLICK_BLOCK &&
            item != null &&
            item.getType() == Material.SHEARS
        ) {
            handleRemove(crop, player);
            return;
        }

        // Harvest
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            (item == null || item.getType() == Material.AIR) &&
            !sneaking
        ) {
            handleHarvest(crop, player, block);
            return;
        }

        // Watering (all crop types)
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            item != null &&
            item.getType() == Material.WATER_BUCKET &&
            !sneaking
        ) {
            handleWater(crop, player, item);
            return;
        }

        // Pruning for cannabis
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            item != null &&
            item.getType() == Material.SHEARS &&
            !sneaking
        ) {
            if (
                crop.type == CropType.CANNABIS &&
                (crop.stage == 3 || crop.stage == 4)
            ) {
                handlePrune(crop, player);
            }
            return;
        }

        // Stripping for tobacco
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            (item == null || item.getType() == Material.AIR) &&
            sneaking
        ) {
            if (
                crop.type == CropType.TOBACCO &&
                (crop.stage == 2 || crop.stage == 4 || crop.stage == 5)
            ) {
                handleStrip(crop, player);
            }
            return;
        }

        // Inspect (shift + right-click)
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            sneaking
        ) {
            handleInspect(crop, player);
            return;
        }

        // Soil enrichment (right-click farmland with compost)
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            block.getType() == Material.FARMLAND &&
            item != null &&
            ItemFactory.isCompost(item)
        ) {
            handleSoilEnrichment(player, block, item);
            return;
        }

        // Crop Journal (right-click crop with book)
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            item != null &&
            (item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.BOOK)
        ) {
            handleCropJournal(player, crop);
            event.setCancelled(true);
            return;
        }

        // Harvest Basket (right-click air with basket)
        if (
            event.getAction() == Action.RIGHT_CLICK_AIR &&
            item != null &&
            ItemFactory.isHarvestBasket(item)
        ) {
            handleDumpBasket(player, item);
            return;
        }
    }

    private void handleHarvest(CropRecord crop, Player player, Block block) {
        if (!isHarvestStage(crop)) return;

        if (crop.type == CropType.CANNABIS && crop.strainId != null && strainManager != null) {
            strainManager.addDiscoveredStrain(player.getUniqueId(), crop.strainId);
        }

        int yield = calculateYield(crop);
        ItemStack drop = getHarvestDrop(crop, yield);
        player.getInventory().addItem(drop);

        if (crop.type == CropType.MUSHROOM) {
            mushroomRightClicks.remove(block.getLocation());
        }

        int seedCount = 1 + (int)(Math.random() * 2);
        for (int i = 0; i < seedCount; i++) {
            ItemStack seed;
            if (crop.type == CropType.CANNABIS && crop.strainId != null) {
                seed = ItemFactory.createCannabisSeed(crop.strainId, crop.strainName);
            } else if (crop.type == CropType.MUSHROOM) {
                seed = ItemFactory.createMushroomSeed();
            } else {
                seed = switch (crop.type) {
                    case CANNABIS -> ItemFactory.createCannabisSeed();
                    case TOBACCO -> ItemFactory.createTobaccoSeed();
                    case TEA -> ItemFactory.createTeaSeed();
                    default -> ItemFactory.createMushroomSeed();
                };
            }
            player.getInventory().addItem(seed);
        }

        player
            .getWorld()
            .playSound(
                block.getLocation(),
                "minecraft:block.crop.break",
                1.0f,
                1.0f
            );
        player
            .getWorld()
            .playSound(
                block.getLocation(),
                "minecraft:block.grass.break",
                1.0f,
                1.0f
            );
        player
            .getWorld()
            .spawnParticle(
                org.bukkit.Particle.VILLAGER_HAPPY,
                block.getLocation().add(0.5, 0.5, 0.5),
                8,
                0.5,
                0.5,
                0.5,
                0
            );

        block.setType(Material.AIR);

        if (soilManager != null && crop.type == CropType.CANNABIS) {
            Location farmlandLoc = block.getLocation().subtract(0, 1, 0);
            soilManager.degrade(farmlandLoc);
        }

        animator.reveal(
            player,
            "§2Harvested " + yield + " " + getDropName(crop) + " + " + seedCount + " seed" + (seedCount > 1 ? "s" : ""),
            null
        );
    }

    private void handleWater(CropRecord crop, Player player, ItemStack bucket) {
        crop.lastWatered = System.currentTimeMillis();
        crop.dirty = true;
        bucket.setType(Material.BUCKET);
        player
            .getWorld()
            .playSound(
                crop.location,
                "minecraft:item.bucket.empty",
                1.0f,
                1.0f
            );
        animator.reveal(player, "§bWatered", null);
    }

    private void handleMist(CropRecord crop, Player player, ItemStack bottle) {
        crop.lastMisted = System.currentTimeMillis();
        crop.flags.remove(CropFlag.NEEDS_MISTING);
        crop.dirty = true;
        bottle.setType(Material.GLASS_BOTTLE);
        player
            .getWorld()
            .playSound(
                crop.location,
                "minecraft:block.pointed_dripstone.drip_water_into_cauldron",
                1.4f,
                1.0f
            );
        player
            .getWorld()
            .spawnParticle(
                org.bukkit.Particle.WATER_SPLASH,
                crop.location.clone().add(0.5, 1, 0.5),
                10,
                0.2,
                0.2,
                0.2,
                0
            );
        animator.reveal(player, "§b❋ Misted", null);
    }

    private void handlePrune(CropRecord crop, Player player) {
        crop.flags.remove(CropFlag.NEEDS_PRUNING);
        crop.lastPruned = System.currentTimeMillis();
        crop.dirty = true;
        player
            .getWorld()
            .playSound(
                crop.location,
                "minecraft:entity.sheep.shear",
                1.2f,
                1.0f
            );
        animator.reveal(player, "§2✂ Pruned — growing well", null);
    }

    private void handleStrip(CropRecord crop, Player player) {
        crop.flags.remove(CropFlag.NEEDS_STRIPPING);
        crop.lastStripped = System.currentTimeMillis();
        crop.dirty = true;
        ItemStack drop = ItemFactory.createWetTobaccoLeaf();
        player.getInventory().addItem(drop);
        player
            .getWorld()
            .playSound(
                crop.location,
                "minecraft:block.azalea_leaves.break",
                1.3f,
                1.0f
            );
        animator.reveal(player, "§6✦ Leaves stripped", null);
    }

    private void handleSoilEnrichment(Player player, Block farmland, ItemStack compost) {
        if (soilManager == null) {
            animator.reveal(player, "§cSoil enrichment not available", null);
            return;
        }

        Location farmLoc = farmland.getLocation();
        int currentLevel = soilManager.getEnrichment(farmLoc);

        if (currentLevel >= 3) {
            animator.reveal(player, "§e§lSoil already max enriched", null);
            return;
        }

        soilManager.setEnrichment(farmLoc, currentLevel + 1);
        compost.setAmount(compost.getAmount() - 1);

        int newLevel = currentLevel + 1;
        String enrichmentStr = "§a" + "✦".repeat(newLevel) + "✧".repeat(3 - newLevel) + " Soil enriched";

        player.getWorld().playSound(farmland.getLocation(), org.bukkit.Sound.BLOCK_GRAVEL_PLACE, 1.0f, 1.0f);
        player.getWorld().playSound(farmland.getLocation(), org.bukkit.Sound.BLOCK_COMPOSTER_FILL, 1.0f, 1.0f);
        player.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, farmland.getLocation().add(0.5, 1, 0.5), 8, 0.3, 0.3, 0.3, 0);

        animator.reveal(player, enrichmentStr, null);
    }

    private void handleCropJournal(Player player, CropRecord crop) {
        ItemStack book = player.getInventory().getItemInMainHand();
        org.bukkit.inventory.meta.BookMeta meta = (org.bukkit.inventory.meta.BookMeta) book.getItemMeta();
        if (meta == null) return;

        String title = "Crop Journal: " + crop.type.name();
        meta.setTitle(title);
        meta.setAuthor("Cultivar");

        StringBuilder content = new StringBuilder();
        content.append("=== Crop Journal ===\n\n");
        content.append("Type: ").append(crop.type.name()).append("\n");
        if (crop.strainName != null) {
            content.append("Strain: ").append(crop.strainName).append("\n");
        }
        content.append("Stage: ").append(crop.stage).append("/").append(crop.type.getMaxStage()).append("\n");
        content.append("Stress: ").append(crop.stress).append("\n");
        content.append("Planted: ").append(new java.util.Date(crop.plantedAt)).append("\n");
        
        if (!crop.flags.isEmpty()) {
            content.append("Flags: ");
            for (CropFlag flag : crop.flags) {
                content.append(flag.name()).append(", ");
            }
            content.append("\n");
        }
        
        if (crop.heatBonus) content.append("Heat Bonus: Yes\n");
        if (crop.waterSourceBonus) content.append("Water Bonus: Yes\n");
        if (crop.coldBiome) content.append("Cold Biome: Yes\n");
        
        content.append("\n=== History ===\n");
        for (String entry : crop.history) {
            content.append("- ").append(entry).append("\n");
        }

        meta.setPages(content.toString());
        book.setItemMeta(meta);

        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
        animator.reveal(player, "§6📖 Journal updated", null);
    }

    private void handleDumpBasket(Player player, ItemStack basket) {
        var pdc = basket.getItemMeta().getPersistentDataContainer();
        NamespacedKey basketKey = new NamespacedKey("cultivar", "basket_contents");
        
        if (!pdc.has(basketKey, PersistentDataType.STRING)) {
            animator.reveal(player, "§eBasket is empty", null);
            return;
        }
        
        String contents = pdc.get(basketKey, PersistentDataType.STRING);
        if (contents == null || contents.isEmpty()) {
            animator.reveal(player, "§eBasket is empty", null);
            return;
        }
        
        String[] items = contents.split(";");
        int dumped = 0;
        
        for (String itemData : items) {
            String[] parts = itemData.split(":");
            if (parts.length != 2) continue;
            
            try {
                String materialName = parts[0];
                int amount = Integer.parseInt(parts[1]);
                Material mat = Material.getMaterial(materialName);
                if (mat != null) {
                    ItemStack stack = new ItemStack(mat, amount);
                    java.util.HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(stack);
                    dumped += amount - remaining.size();
                }
            } catch (Exception e) {
                // skip invalid entries
            }
        }
        
        pdc.remove(basketKey);
        basket.setItemMeta(basket.getItemMeta());
        
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ITEM_BUNDLE_DROP_CONTENTS, 1.0f, 1.0f);
        animator.reveal(player, "§aDumped " + dumped + " items from basket", null);
    }

    private void handleInspect(CropRecord crop, Player player) {
        if (crop.deathReason != null) {
            animator.instant(player, "§c✧ Dead: " + crop.deathReason + " — double-left-click to clean up");
            return;
        }

        long now = System.currentTimeMillis();
        long stageTime = crop.getStageTimeMs(plugin.getConfig(), 0);
        long timeToAdvance = (crop.stageAdvancedAt + stageTime) - now;

        long waterExpiry = plugin.getConfig().getLong("cultivar." + crop.type.name().toLowerCase() + ".water-expiry-minutes", 45) * 60000;
        boolean watered = now - crop.lastWatered <= waterExpiry;
        org.bukkit.World world = crop.location.getWorld();
        boolean rained = world.hasStorm() && world.getHighestBlockYAt(crop.location) <= crop.location.getBlockY();
        boolean isWatered = watered || rained;
        int light = crop.location.getBlock().getLightLevel();

        int minLight = switch (crop.type) {
            case CANNABIS -> 10;
            case TOBACCO -> crop.flags.contains(CropFlag.LOW_LIGHT) ? 0 : 12; // Allow low if flagged
            case TEA -> 7;
            case MUSHROOM -> 0;
        };
        int maxLight = switch (crop.type) {
            case TEA -> 13;
            default -> 15;
        };
        boolean lightOk = light >= minLight && light <= maxLight;

        String wateredStr = isWatered ? "§bWatered" : "§cDry";
        String lightStr = "§eLight: " + light;
        String readyStr = (timeToAdvance <= 0) ? " §a[Ready]" : "";
        String strainStr = (crop.strainName != null) ? " §7[" + crop.strainName + "]" : "";

        String status = "§a" + crop.type.name().toLowerCase() + strainStr + " stage " + crop.stage + " stress " + crop.stress + " [" + wateredStr + "§a] " + lightStr + readyStr;

        // Show specific blockers if ready but not advancing
        if (timeToAdvance <= 0) {
            if (!isWatered) {
                status += " §cNeeds Water";
            }
            if (!lightOk) {
                status += " §cLow Light";
            }
            if (crop.flags.contains(CropFlag.DYING)) {
                status += " §cDying";
            }
            if (crop.type == CropType.CANNABIS && (crop.stage == 3 || crop.stage == 4) && crop.flags.contains(CropFlag.NEEDS_PRUNING)) {
                status += " §cPaused: Pruning Needed";
            }
            if (crop.type == CropType.TOBACCO && (crop.stage == 2 || crop.stage == 4 || crop.stage == 5) && crop.flags.contains(CropFlag.NEEDS_STRIPPING)) {
                status += " §cPaused: Stripping Needed";
            }
            if (crop.type == CropType.TEA && crop.flags.contains(CropFlag.NEEDS_MISTING)) {
                status += " §cPaused: Misting Needed";
            }
        }

        if (!crop.flags.isEmpty()) {
            status += " flags: " + crop.flags.stream().map(Enum::name).collect(Collectors.joining(", "));
        }
        if (timeToAdvance > 0) {
            status += " next advance in " + (timeToAdvance / 1000) + "s";
        }
        animator.instant(player, status);
    }

    private void handleRemove(CropRecord crop, Player player) {
        org.bukkit.Location loc = crop.location;
        java.util.UUID key = player.getUniqueId();

        if (pendingRemovals.containsKey(key) && pendingRemovals.get(key).equals(loc)) {
            pendingRemovals.remove(key);

            try {
                cropManager.remove(loc);
            } catch (Exception e) {
                e.printStackTrace();
            }

            loc.getBlock().setType(Material.AIR);

            player
                .getWorld()
                .playSound(
                    loc,
                    "minecraft:block.grass.break",
                    1.0f,
                    1.0f
                );

            animator.reveal(player, "§c✂ Plant removed", null);
        } else {
            pendingRemovals.put(key, loc);
            animator.reveal(player, "§e⚠ Left-click again to confirm removal", null);
        }
    }

    private boolean isHarvestStage(CropRecord crop) {
        return switch (crop.type) {
            case CANNABIS -> crop.stage >= 4;
            case TOBACCO -> crop.stage >= 5;
            case TEA -> crop.stage >= 3;
            case MUSHROOM -> crop.stage >= 3;
        };
    }

    private int calculateYield(CropRecord crop) {
        int base = switch (crop.type) {
            case CANNABIS -> 3;
            case TOBACCO -> 5;
            case TEA -> 4;
            case MUSHROOM -> 2;
        };

        int yield = base;

        if (crop.type == CropType.CANNABIS) {
            if (crop.stress > 2) {
                yield -= (crop.stress - 2);
            }
            if (crop.flags.contains(CropFlag.OVERGROWN)) {
                yield -= 1;
            }
        }

        if (crop.type == CropType.TOBACCO) {
            if (crop.flags.contains(CropFlag.STUNTED)) {
                yield -= 1;
            }
        }

        return Math.max(1, yield);
    }

    private ItemStack getHarvestDrop(CropRecord crop, int yield) {
        ItemStack item = switch (crop.type) {
            case CANNABIS -> ItemFactory.createCannabisBud();
            case TOBACCO -> ItemFactory.createWetTobaccoLeaf();
            case TEA -> ItemFactory.createFreshTeaLeaf();
            case MUSHROOM -> ItemFactory.createDriedMushroom();
        };
        item.setAmount(yield);
        return item;
    }

    private String getDropName(CropRecord crop) {
        return switch (crop.type) {
            case CANNABIS -> "cannabis buds";
            case TOBACCO -> "wet tobacco leaves";
            case TEA -> "fresh tea leaves";
            case MUSHROOM -> "dried mushrooms";
        };
    }
}
