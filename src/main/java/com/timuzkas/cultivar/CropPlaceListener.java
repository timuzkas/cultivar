package com.timuzkas.cultivar;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class CropPlaceListener implements Listener {
    private final CropManager cropManager;
    private final ActionBarAnimator animator;

    public CropPlaceListener(CropManager cropManager, ActionBarAnimator animator) {
        this.cropManager = cropManager;
        this.animator = animator;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();

        if (block == null || item == null) return;

        Material blockType = block.getType();
        boolean isFarmland = blockType == Material.FARMLAND;
        boolean isFlowerPot = blockType == Material.FLOWER_POT;

        CropType type = null;
        if (ItemFactory.isCannabisSeed(item) && isFarmland) {
            type = CropType.CANNABIS;
        } else if (ItemFactory.isTobaccoSeed(item) && isFarmland) {
            type = CropType.TOBACCO;
        } else if (ItemFactory.isTeaSeed(item) && (isFarmland || isFlowerPot)) {
            type = CropType.TEA;
        }

        if (type != null) {
            event.setCancelled(true);
            // Consume seed
            item.setAmount(item.getAmount() - 1);
            // Place visual block above the clicked block
            Material visualBlock = getVisualBlock(type, 0);
            Block placeBlock = block.getRelative(org.bukkit.block.BlockFace.UP);
            placeBlock.setType(visualBlock);
            // Register crop
            CropRecord record = new CropRecord();
            record.id = UUID.randomUUID().toString();
            record.ownerUuid = player.getUniqueId();
            record.location = placeBlock.getLocation();
            record.type = type;
            record.stage = 0;
            record.plantedAt = System.currentTimeMillis();
            record.stageAdvancedAt = System.currentTimeMillis();
            // Set heat bonus for tobacco
            if (type == CropType.TOBACCO) {
                record.heatBonus = checkHeatBonus(record.location);
            }
            // For tea, check water source and set lastMisted
            if (type == CropType.TEA) {
                record.waterSourceBonus = checkWaterSource(record.location);
                record.lastMisted = System.currentTimeMillis();
            }
            try {
                cropManager.register(record);
            } catch (Exception e) {
                e.printStackTrace();
            }
            animator.reveal(player, "§2Planted " + type.name().toLowerCase() + " seed", null);
        }
    }

    private Material getVisualBlock(CropType type, int stage) {
        return type.getVisualBlock(stage);
    }

    private boolean checkHeatBonus(org.bukkit.Location location) {
        // Check 4 block radius for campfire etc.
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    Block b = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z);
                    Material m = b.getType();
                    if (m == Material.CAMPFIRE || m == Material.SOUL_CAMPFIRE || m == Material.SMOKER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkWaterSource(org.bukkit.Location location) {
        // Check 3 block radius for water source
        int radius = 3;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block b = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z);
                    if (b.getType() == Material.WATER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}