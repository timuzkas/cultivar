package com.timuzkas.cultivar;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import java.util.stream.Collectors;

public class CropInteractListener implements Listener {

    private final CropManager cropManager;
    private final ActionBarAnimator animator;
    private final org.bukkit.plugin.Plugin plugin;
    private final java.util.Map<java.util.UUID, org.bukkit.Location> pendingRemovals = new java.util.HashMap<>();

    public CropInteractListener(
        CropManager cropManager,
        ActionBarAnimator animator,
        org.bukkit.plugin.Plugin plugin
    ) {
        this.cropManager = cropManager;
        this.animator = animator;
        this.plugin = plugin;
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
    }

    private void handleHarvest(CropRecord crop, Player player, Block block) {
        if (!isHarvestStage(crop)) return;

        int yield = calculateYield(crop);
        ItemStack drop = getHarvestDrop(crop, yield);
        player.getInventory().addItem(drop);

        ItemStack seed = switch (crop.type) {
            case CANNABIS -> ItemFactory.createCannabisSeed();
            case TOBACCO -> ItemFactory.createTobaccoSeed();
            case TEA -> ItemFactory.createTeaSeed();
        };
        player.getInventory().addItem(seed);

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

        try {
            cropManager.remove(block.getLocation());
        } catch (Exception e) {
            e.printStackTrace();
        }

        block.setType(Material.AIR);

        animator.reveal(
            player,
            "§2Harvested " + yield + " " + getDropName(crop) + " + 1 seed",
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

    private void handleInspect(CropRecord crop, Player player) {
        if (crop.deathReason != null) {
            animator.instant(player, "§c✧ Dead: " + crop.deathReason + " — double-left-click to clean up");
            return;
        }

        long now = System.currentTimeMillis();
        long stageTime = crop.getStageTimeMs(plugin.getConfig());
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
        };
        int maxLight = crop.type == CropType.TEA ? 13 : 15;
        boolean lightOk = light >= minLight && light <= maxLight;

        String wateredStr = isWatered ? "§bWatered" : "§cDry";
        String lightStr = "§eLight: " + light;
        String readyStr = (timeToAdvance <= 0) ? " §a[Ready]" : "";

        String status = "§a" + crop.type.name().toLowerCase() + " stage " + crop.stage + " stress " + crop.stress + " [" + wateredStr + "§a] " + lightStr + readyStr;

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
        };
    }

    private int calculateYield(CropRecord crop) {
        int base = switch (crop.type) {
            case CANNABIS -> 3;
            case TOBACCO -> 5;
            case TEA -> 4;
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
        };
        item.setAmount(yield);
        return item;
    }

    private String getDropName(CropRecord crop) {
        return switch (crop.type) {
            case CANNABIS -> "cannabis buds";
            case TOBACCO -> "wet tobacco leaves";
            case TEA -> "fresh tea leaves";
        };
    }
}
