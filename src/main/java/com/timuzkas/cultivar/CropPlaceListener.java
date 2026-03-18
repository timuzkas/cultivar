package com.timuzkas.cultivar;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.UUID;

import org.bukkit.block.BlockFace;

public class CropPlaceListener implements Listener {
    private final CropManager cropManager;
    private final ActionBarAnimator animator;
    private final org.bukkit.plugin.Plugin plugin;

    public CropPlaceListener(CropManager cropManager, ActionBarAnimator animator, org.bukkit.plugin.Plugin plugin) {
        this.cropManager = cropManager;
        this.animator = animator;
        this.plugin = plugin;
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
        String strainId = null;
        String strainName = null;

        if (ItemFactory.isCannabisSeed(item) && isFarmland) {
            type = CropType.CANNABIS;
            strainId = ItemFactory.getStrainId(item);
            strainName = ItemFactory.getStrainName(item);
        } else if (ItemFactory.isTobaccoSeed(item) && isFarmland) {
            type = CropType.TOBACCO;
        } else if (ItemFactory.isTeaSeed(item) && (isFarmland || isFlowerPot)) {
            type = CropType.TEA;
        } else if (ItemFactory.isMushroomSeed(item) && isFarmland) {
            type = CropType.MUSHROOM;
        }

        if (type != null) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            Material visualBlock = getVisualBlock(type, 0);
            Block placeBlock = block.getRelative(BlockFace.UP);
            placeBlock.setType(visualBlock);

            CropRecord record = new CropRecord();
            record.id = UUID.randomUUID().toString();
            record.ownerUuid = player.getUniqueId();
            record.location = placeBlock.getLocation();
            record.type = type;
            record.stage = 0;
            record.plantedAt = System.currentTimeMillis();
            record.stageAdvancedAt = System.currentTimeMillis();

            if (type == CropType.CANNABIS) {
                if (strainId == null) {
                    StrainProfile strain = StrainProfile.generate(record.id);
                    record.strainId = strain.strainId;
                    record.strainName = strain.name;
                } else {
                    record.strainId = strainId;
                    record.strainName = strainName;
                }
            }

            if (type == CropType.TOBACCO) {
                record.heatBonus = checkHeatBonus(record.location);
                record.coldBiome = checkColdBiome(record.location);
            }

            if (type == CropType.TEA) {
                record.waterSourceBonus = checkWaterSource(record.location);
                record.lastMisted = System.currentTimeMillis();
            }

            try {
                cropManager.register(record);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String strainMsg = (type == CropType.CANNABIS && record.strainName != null) 
                ? " §7(" + record.strainName + ")" : "";
            animator.reveal(player, "§2Planted " + type.name().toLowerCase() + " seed" + strainMsg, null);
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

    private boolean checkColdBiome(org.bukkit.Location location) {
        org.bukkit.block.Block block = location.getBlock();
        if (org.bukkit.block.Block.class.getCanonicalName() == null) return false;
        String biomeName = location.getWorld().getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ()).name();
        List<String> coldBiomes = plugin.getConfig().getStringList("cultivar.tobacco.cold-biomes");
        if (coldBiomes.isEmpty()) {
            coldBiomes = List.of("SNOWY_TAIGA", "FROZEN_PEAKS", "FROZEN_RIVER", "ICE_SPIKES", "SNOWY_PLAINS", 
                                 "SNOWY_BEACH", "SNOWY_MOUNTAINS", "GROVE", "JAGGED_PEAKS", "STONY_PEAKS");
        }
        return coldBiomes.contains(biomeName);
    }
}