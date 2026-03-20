package com.timuzkas.cultivar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CropInteractListener implements Listener {

    private final CropManager cropManager;
    private final ActionBarAnimator animator;
    private final org.bukkit.plugin.Plugin plugin;
    private final java.util.Map<
        java.util.UUID,
        org.bukkit.Location
    > pendingRemovals = new java.util.HashMap<>();
    private final java.util.Map<
        org.bukkit.Location,
        Integer
    > mushroomRightClicks = new java.util.HashMap<>();
    private SoilManager soilManager;
    private PlayerStrainManager strainManager;
    private HarvestBasketManager basketManager;
    private GrowerManager growerManager;

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

    public void setBasketManager(HarvestBasketManager basketManager) {
        this.basketManager = basketManager;
    }

    public void setGrowerManager(GrowerManager growerManager) {
        this.growerManager = growerManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (
            event.getAction() != Action.RIGHT_CLICK_BLOCK &&
            event.getAction() != Action.LEFT_CLICK_BLOCK &&
            event.getAction() != Action.RIGHT_CLICK_AIR
        ) return;

        Player player = event.getPlayer();
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        boolean sneaking = player.isSneaking();

        if (
            event.getAction() == Action.RIGHT_CLICK_AIR &&
            item != null &&
            ItemFactory.isHarvestBasket(item)
        ) {
            handleDumpBasket(player, item);
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) return;

        CropRecord crop = cropManager.getByLocation(block.getLocation());
        if (crop == null) return;

        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            crop.type == CropType.MUSHROOM
        ) {
            int clickCount =
                mushroomRightClicks.getOrDefault(block.getLocation(), 0) + 1;
            mushroomRightClicks.put(block.getLocation(), clickCount);
            if (clickCount > 3) {
                crop.stress += 1;
                crop.flags.add(CropFlag.STUNTED);
                crop.dirty = true;
                animator.reveal(
                    player,
                    "§c⚠ Stop fussing — mushrooms hate it",
                    null
                );
                mushroomRightClicks.remove(block.getLocation());
            }
        }
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
            item != null &&
            ItemFactory.isHarvestBasket(item) &&
            !sneaking
        ) {
            event.setCancelled(true);
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);

            handleBasketHarvest(crop, player, block, item);
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
            item != null &&
            item.getType() == Material.SHEARS &&
            !sneaking &&
            crop.type == CropType.TOBACCO &&
            (crop.stage == 2 || crop.stage == 4 || crop.stage == 5)
        ) {
            handleStrip(crop, player);
            return;
        }

        // Inspect (shift + right-click)
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && sneaking) {
            handleInspect(crop, player);
            return;
        }

        // Crop Journal (right-click crop with book)
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            item != null &&
            (item.getType() == Material.WRITTEN_BOOK ||
                item.getType() == Material.BOOK)
        ) {
            handleCropJournal(player, crop);
            event.setCancelled(true);
            return;
        }

        // Harvest Basket (right-click with basket - air or block, with or without shift)
        if (
            (event.getAction() == Action.RIGHT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
            item != null &&
            ItemFactory.isHarvestBasket(item)
        ) {
            handleDumpBasket(player, item);
            return;
        }

        // Bare-hand harvest (right-click crop at harvest stage)
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            (item == null || item.getType() == Material.AIR) &&
            !sneaking &&
            isHarvestStage(crop)
        ) {
            handleHarvest(crop, player, block);
            return;
        }

        // Inspect (shift + right-click)
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && sneaking) {
            handleInspect(crop, player);
            return;
        }

        // Bare-hand harvest (right-click crop at harvest stage)
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            (item == null || item.getType() == Material.AIR) &&
            !sneaking &&
            isHarvestStage(crop)
        ) {
            handleHarvest(crop, player, block);
            return;
        }
    }

    private void handleHarvest(CropRecord crop, Player player, Block block) {
        if (!isHarvestStage(crop)) return;

        if (strainManager != null) {
            strainManager.loadPlayerStrains(player.getUniqueId());
        }
        
        if (growerManager != null) {
            growerManager.loadPlayer(player.getUniqueId(), player.getName());
        }

        String strainIdToSave = crop.strainId;
        if (strainIdToSave == null) {
            strainIdToSave = "wild_" + crop.type.name().toLowerCase();
        }
        
        if (strainManager != null) {
            strainManager.addDiscoveredStrain(
                player.getUniqueId(),
                strainIdToSave,
                crop.type
            );
            
            Set<String> discovered = strainManager.getDiscoveredStrains(player.getUniqueId());
            if (discovered.contains(strainIdToSave)) {
                StrainProfile strain = StrainProfile.generate(strainIdToSave, crop.type);
                String typeName = switch (crop.type) {
                    case CANNABIS -> "Strain";
                    case TOBACCO -> "Varietal";
                    case TEA -> "Cultivar";
                    case MUSHROOM -> "Strain";
                    default -> "Strain";
                };
                animator.reveal(player, "§6✦ New " + typeName + ": " + strain.name, null);
            }
        }

        int yield = calculateYield(crop);
        ItemStack drop = getHarvestDrop(crop, yield);
        player.getInventory().addItem(drop);

        if (crop.type == CropType.MUSHROOM) {
            mushroomRightClicks.remove(block.getLocation());
        }

        boolean isOwner = crop.ownerUuid.equals(player.getUniqueId());
        String breederUuid = crop.originalBreederUuid != null ? crop.originalBreederUuid.toString() : null;
        String breederName = crop.originalBreederName;
        if (breederUuid == null && crop.strainId != null) {
            breederUuid = crop.ownerUuid.toString();
            breederName = player.getName();
        }

        int seedCount = 1 + (int) (Math.random() * 2);
        float seedQuality = 0.0f;
        if (growerManager != null && crop.type == CropType.CANNABIS) {
            seedQuality = growerManager.getSeedQualityBonus(player.getUniqueId());
        }
        
        int sporeBonus = 0;
        if (crop.strainId != null && crop.type == CropType.MUSHROOM) {
            StrainProfile strain = StrainProfile.generate(crop.strainId, CropType.MUSHROOM);
            sporeBonus = strain.sporeDensity - 1;
        }
        int totalSeeds = seedCount + sporeBonus;
        
        for (int i = 0; i < totalSeeds; i++) {
            ItemStack seed;
            if (crop.type == CropType.CANNABIS && crop.strainId != null) {
                seed = ItemFactory.createCannabisSeed(
                    crop.strainId,
                    crop.strainName,
                    breederUuid,
                    breederName,
                    seedQuality
                );
            } else if (crop.type == CropType.TOBACCO && crop.strainId != null) {
                seed = ItemFactory.createTobaccoSeed(crop.strainId, crop.strainName, breederUuid, breederName);
            } else if (crop.type == CropType.TEA && crop.strainId != null) {
                seed = ItemFactory.createTeaSeed(crop.strainId, crop.strainName, breederUuid, breederName);
            } else if (crop.type == CropType.MUSHROOM && crop.strainId != null) {
                seed = ItemFactory.createMushroomSeed(crop.strainId, crop.strainName, breederUuid, breederName);
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
        try {
            cropManager.remove(crop.location);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (soilManager != null && crop.type == CropType.CANNABIS) {
            Location farmlandLoc = block.getLocation().subtract(0, 1, 0);
            soilManager.degrade(farmlandLoc);
        }

        if (growerManager != null) {
            int harvestScore = growerManager.getEventScore("harvest");
            if (harvestScore > 0) {
                String newTitle = growerManager.addScore(player.getUniqueId(), player.getName(), harvestScore);
                if (newTitle != null) {
                    animator.reveal(player, "§6✦ Grower rank: " + newTitle, null);
                }
            }

            if (!isOwner && crop.originalBreederUuid != null && !crop.originalBreederUuid.equals(player.getUniqueId())) {
                int otherStrainScore = growerManager.getEventScore("harvest-other-strain");
                if (otherStrainScore > 0) {
                    growerManager.addScore(crop.originalBreederUuid, crop.originalBreederName, otherStrainScore);
                }
            }

            if (crop.stress == 0) {
                int zeroStressScore = growerManager.getEventScore("zero-stress-harvest");
                if (zeroStressScore > 0) {
                    String newTitle = growerManager.addScore(player.getUniqueId(), player.getName(), zeroStressScore);
                    if (newTitle != null) {
                        animator.reveal(player, "§6✦ Grower rank: " + newTitle, null);
                    }
                }
            }
        }

        animator.reveal(
            player,
            "§2Harvested " +
                yield +
                " " +
                getDropName(crop) +
                " + " +
                seedCount +
                " seed" +
                (seedCount > 1 ? "s" : ""),
            null
        );
    }

    private void handleBasketHarvest(
        CropRecord crop,
        Player player,
        Block block,
        ItemStack basket
    ) {
        if (!isHarvestStage(crop)) return;

        if (strainManager != null) {
            strainManager.loadPlayerStrains(player.getUniqueId());
        }

        String strainIdToSave = crop.strainId;
        if (strainIdToSave == null) {
            strainIdToSave = "wild_" + crop.type.name().toLowerCase();
        }
        
        if (strainManager != null) {
            strainManager.addDiscoveredStrain(
                player.getUniqueId(),
                strainIdToSave,
                crop.type
            );
            
            Set<String> discovered = strainManager.getDiscoveredStrains(player.getUniqueId());
            if (discovered.contains(strainIdToSave)) {
                StrainProfile strain = StrainProfile.generate(strainIdToSave, crop.type);
                String typeName = switch (crop.type) {
                    case CANNABIS -> "Strain";
                    case TOBACCO -> "Varietal";
                    case TEA -> "Cultivar";
                    case MUSHROOM -> "Strain";
                    default -> "Varietal";
                };
                animator.reveal(player, "§6✦ New " + typeName + ": " + strain.name, null);
            }
        }

        int yield = calculateYield(crop);
        ItemStack drop = getHarvestDrop(crop, yield);

        addToBasket(player, basket, drop);

        if (crop.type == CropType.MUSHROOM) {
            mushroomRightClicks.remove(block.getLocation());
        }

        int seedCount = 1 + (int) (Math.random() * 2);
        
        int sporeBonus = 0;
        if (crop.strainId != null && crop.type == CropType.MUSHROOM) {
            StrainProfile strain = StrainProfile.generate(crop.strainId, CropType.MUSHROOM);
            sporeBonus = strain.sporeDensity - 1;
        }
        int totalSeeds = seedCount + sporeBonus;
        
        for (int i = 0; i < totalSeeds; i++) {
            ItemStack seed;
            String breederUuid = crop.originalBreederUuid != null ? crop.originalBreederUuid.toString() : null;
            String breederName = crop.originalBreederName;
            if (breederUuid == null && crop.strainId != null) {
                breederUuid = crop.ownerUuid.toString();
                breederName = player.getName();
            }
            
            if (crop.type == CropType.CANNABIS && crop.strainId != null) {
                float seedQuality = 0.0f;
                if (growerManager != null) {
                    seedQuality = growerManager.getSeedQualityBonus(player.getUniqueId());
                }
                seed = ItemFactory.createCannabisSeed(crop.strainId, crop.strainName, breederUuid, breederName, seedQuality);
            } else if (crop.type == CropType.TOBACCO && crop.strainId != null) {
                seed = ItemFactory.createTobaccoSeed(crop.strainId, crop.strainName, breederUuid, breederName);
            } else if (crop.type == CropType.TEA && crop.strainId != null) {
                seed = ItemFactory.createTeaSeed(crop.strainId, crop.strainName, breederUuid, breederName);
            } else if (crop.type == CropType.MUSHROOM && crop.strainId != null) {
                seed = ItemFactory.createMushroomSeed(crop.strainId, crop.strainName, breederUuid, breederName);
            } else {
                seed = switch (crop.type) {
                    case CANNABIS -> ItemFactory.createCannabisSeed();
                    case TOBACCO -> ItemFactory.createTobaccoSeed();
                    case TEA -> ItemFactory.createTeaSeed();
                    default -> ItemFactory.createMushroomSeed();
                };
            }
            addToBasket(player, basket, seed);
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
        try {
            cropManager.remove(crop.location);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (soilManager != null && crop.type == CropType.CANNABIS) {
            Location farmlandLoc = block.getLocation().subtract(0, 1, 0);
            soilManager.degrade(farmlandLoc);
        }

        animator.reveal(player, "§6+ Harvested to basket", null);
    }

    private void addToBasket(Player player, ItemStack basket, ItemStack item) {
        if (basketManager != null) {
            basketManager.addToBasket(player, basket, item);
        }
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

        if (growerManager != null) {
            int mistScore = growerManager.getEventScore("mist");
            if (mistScore > 0) {
                String newTitle = growerManager.addScore(player.getUniqueId(), player.getName(), mistScore);
                if (newTitle != null) {
                    animator.reveal(player, "§6✦ Grower rank: " + newTitle + " §b❋ Misted", null);
                    return;
                }
            }
        }

        animator.reveal(player, "§b❋ Misted", null);
    }

    private void handlePrune(CropRecord crop, Player player) {
        if (!crop.flags.contains(CropFlag.NEEDS_PRUNING)) {
            animator.reveal(player, "§cNothing to prune", null);
            return;
        }
        crop.flags.remove(CropFlag.NEEDS_PRUNING);
        crop.lastPruned = System.currentTimeMillis();
        crop.dirty = true;

        int trimAmount = 1 + (int) (Math.random() * 2);
        ItemStack trim;
        if (crop.strainId != null) {
            trim = ItemFactory.createCannabisTrim(
                crop.strainId,
                crop.strainName
            );
        } else {
            trim = ItemFactory.createCannabisTrim();
        }
        trim.setAmount(trimAmount);
        player.getInventory().addItem(trim);

        player
            .getWorld()
            .playSound(
                crop.location,
                "minecraft:entity.sheep.shear",
                1.2f,
                1.0f
            );

        if (growerManager != null) {
            int pruneScore = growerManager.getEventScore("prune");
            if (pruneScore > 0) {
                String newTitle = growerManager.addScore(player.getUniqueId(), player.getName(), pruneScore);
                if (newTitle != null) {
                    animator.reveal(player, "§6✦ Grower rank: " + newTitle + " §2✂ Pruned — " + trimAmount + " trim", null);
                    return;
                }
            }
        }

        animator.reveal(player, "§2✂ Pruned — " + trimAmount + " trim", null);
    }

    private void handleStrip(CropRecord crop, Player player) {
        if (!crop.flags.contains(CropFlag.NEEDS_STRIPPING)) {
            animator.reveal(player, "§cNothing to strip", null);
            return;
        }
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

        if (growerManager != null) {
            int stripScore = growerManager.getEventScore("strip");
            if (stripScore > 0) {
                String newTitle = growerManager.addScore(player.getUniqueId(), player.getName(), stripScore);
                if (newTitle != null) {
                    animator.reveal(player, "§6✦ Grower rank: " + newTitle + " §6✦ Leaves stripped", null);
                    return;
                }
            }
        }

        animator.reveal(player, "§6✦ Leaves stripped", null);
    }

    private void handleSoilEnrichment(
        Player player,
        Block farmland,
        ItemStack compost
    ) {
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
        String enrichmentStr =
            "§a" +
            "✦".repeat(newLevel) +
            "✧".repeat(3 - newLevel) +
            " Soil enriched";

        player
            .getWorld()
            .playSound(
                farmland.getLocation(),
                org.bukkit.Sound.BLOCK_GRAVEL_PLACE,
                1.0f,
                1.0f
            );
        player
            .getWorld()
            .playSound(
                farmland.getLocation(),
                org.bukkit.Sound.BLOCK_COMPOSTER_FILL,
                1.0f,
                1.0f
            );
        player
            .getWorld()
            .spawnParticle(
                org.bukkit.Particle.VILLAGER_HAPPY,
                farmland.getLocation().add(0.5, 1, 0.5),
                8,
                0.3,
                0.3,
                0.3,
                0
            );

        animator.reveal(player, enrichmentStr, null);
    }

    private void handleCropJournal(Player player, CropRecord crop) {
        ItemStack book = player.getInventory().getItemInMainHand();
        org.bukkit.inventory.meta.BookMeta meta =
            (org.bukkit.inventory.meta.BookMeta) book.getItemMeta();
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
        content
            .append("Stage: ")
            .append(crop.stage)
            .append("/")
            .append(crop.type.getMaxStage())
            .append("\n");
        content.append("Stress: ").append(crop.stress).append("\n");
        content
            .append("Planted: ")
            .append(new java.util.Date(crop.plantedAt))
            .append("\n");

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

        player
            .getWorld()
            .playSound(
                player.getLocation(),
                org.bukkit.Sound.ITEM_BOOK_PAGE_TURN,
                1.0f,
                1.0f
            );
        animator.reveal(player, "§6📖 Journal updated", null);
    }

    private void handleDumpBasket(Player player, ItemStack basket) {
        if (basketManager != null) {
            basketManager.handleDumpBasket(player, basket);
        }
    }

    private void handleInspect(CropRecord crop, Player player) {
        if (crop.deathReason != null) {
            animator.instant(
                player,
                "§c✧ Dead: " + crop.deathReason + "  §7shears to remove"
            );
            return;
        }

        long now = System.currentTimeMillis();
        int enrichment =
            soilManager != null
                ? soilManager.getEnrichment(
                      crop.location.clone().subtract(0, 1, 0)
                  )
                : 0;
        long stageTime = crop.getStageTimeMs(plugin.getConfig(), enrichment);
        long timeToAdvance = (crop.stageAdvancedAt + stageTime) - now;

        long waterExpiry =
            plugin
                .getConfig()
                .getLong(
                    "cultivar." +
                        crop.type.name().toLowerCase() +
                        ".water-expiry-minutes",
                    45
                ) *
            60000;
        boolean watered =
            (now - crop.lastWatered <= waterExpiry) ||
            (crop.location.getWorld().hasStorm() &&
                crop.location.getWorld().getHighestBlockYAt(crop.location) <=
                crop.location.getBlockY());

        int light = crop.location.getBlock().getLightLevel();
        boolean lightOk = isLightOk(crop, light);

        String strainPart =
            crop.strainName != null ? " §7[" + crop.strainName + "]" : "";
        String stagePart =
            "§astage " + crop.stage + "§7/" + crop.type.getMaxStage();
        String soilPart =
            enrichment > 0
                ? "  §2soil " +
                  "✦".repeat(enrichment) +
                  "§7" +
                  "✧".repeat(3 - enrichment)
                : "";
        String lightColor = lightOk ? "§a" : "§c";
        String line1 =
            "§2" +
            crop.type.name().toLowerCase() +
            strainPart +
            "  " +
            stagePart +
            "  " +
            lightColor +
            "☀ " +
            light +
            soilPart;

        String waterStr = watered ? "§b💧 watered" : "§c💧 dry";
        String stressStr =
            crop.stress == 0
                ? "§astress 0"
                : crop.stress < 3
                    ? "§estress " + crop.stress
                    : "§cstress " + crop.stress;
        String timeStr =
            timeToAdvance <= 0
                ? "§a[ready]"
                : "§7next §e" + formatTime(timeToAdvance);
        String line2 = waterStr + "   " + stressStr + "   " + timeStr;

        String line3 = buildFlagLine(crop, watered, lightOk);

        List<String> lines = new ArrayList<>();
        lines.add(line1);
        lines.add(line2);
        if (!line3.isEmpty()) lines.add(line3);

        animator.sequence(player, lines, 40);
    }

    private boolean isLightOk(CropRecord crop, int light) {
        int min = switch (crop.type) {
            case CANNABIS -> 10;
            case TOBACCO -> crop.flags.contains(CropFlag.LOW_LIGHT) ? 0 : 12;
            case TEA -> 7;
            case MUSHROOM -> 0;
        };
        int max = crop.type == CropType.TEA ? 13 : 15;
        return light >= min && light <= max;
    }

    private String buildFlagLine(
        CropRecord crop,
        boolean watered,
        boolean lightOk
    ) {
        List<String> parts = new ArrayList<>();
        if (!watered) parts.add("§c💧 needs water");
        if (!lightOk) parts.add("§c☀ bad light");
        if (crop.flags.contains(CropFlag.NEEDS_PRUNING)) parts.add("§c✂ prune");
        if (crop.flags.contains(CropFlag.NEEDS_STRIPPING)) parts.add(
            "§c✦ strip"
        );
        if (crop.flags.contains(CropFlag.NEEDS_MISTING)) parts.add("§c❋ mist");
        if (crop.flags.contains(CropFlag.DYING)) parts.add("§4☠ dying");
        if (crop.flags.contains(CropFlag.OVERGROWN)) parts.add("§6⚠ overgrown");
        if (crop.flags.contains(CropFlag.STUNTED)) parts.add("§6⚠ stunted");
        return String.join("  ", parts);
    }

    private String formatTime(long ms) {
        long s = ms / 1000;
        if (s < 60) return s + "s";
        long m = s / 60;
        long rs = s % 60;
        return rs == 0 ? m + "m" : m + "m " + rs + "s";
    }

    private void handleRemove(CropRecord crop, Player player) {
        org.bukkit.Location loc = crop.location;
        java.util.UUID key = player.getUniqueId();

        if (
            pendingRemovals.containsKey(key) &&
            pendingRemovals.get(key).equals(loc)
        ) {
            pendingRemovals.remove(key);

            try {
                cropManager.remove(loc);
            } catch (Exception e) {
                e.printStackTrace();
            }

            loc.getBlock().setType(Material.AIR);

            player
                .getWorld()
                .playSound(loc, "minecraft:block.grass.break", 1.0f, 1.0f);

            animator.reveal(player, "§c✂ Plant removed", null);
        } else {
            pendingRemovals.put(key, loc);
            animator.reveal(
                player,
                "§e⚠ Left-click again to confirm removal",
                null
            );
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
            if (crop.strainId != null) {
                StrainProfile strain = StrainProfile.generate(crop.strainId, CropType.CANNABIS);
                yield = (int) Math.round(yield * (1 + strain.yieldBonus));
            }
        }

        if (crop.type == CropType.TOBACCO) {
            if (crop.flags.contains(CropFlag.STUNTED)) {
                yield -= 1;
            }
            if (crop.strainId != null) {
                StrainProfile strain = StrainProfile.generate(crop.strainId, CropType.TOBACCO);
                yield += strain.leafYieldBonus;
            }
        }

        if (crop.type == CropType.TEA) {
            if (crop.strainId != null) {
                StrainProfile strain = StrainProfile.generate(crop.strainId, CropType.TEA);
                yield = (int) Math.round(yield * (1 + strain.brewStrength));
            }
        }

        if (crop.type == CropType.MUSHROOM) {
            if (crop.strainId != null) {
                StrainProfile strain = StrainProfile.generate(crop.strainId, CropType.MUSHROOM);
                yield += strain.sporeDensity - 1;
            }
        }

        int yieldFloor = 1;
        if (growerManager != null) {
            yieldFloor += growerManager.getYieldFloorBonus(crop.ownerUuid);
        }
        return Math.max(yieldFloor, yield);
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
