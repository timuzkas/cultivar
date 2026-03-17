package com.timuzkas.cultivar;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CropGrowthTask extends BukkitRunnable {
    private final CropManager cropManager;
    private final Plugin plugin;

    public CropGrowthTask(CropManager cropManager, Plugin plugin) {
        this.cropManager = cropManager;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        for (CropRecord crop : cropManager.getAll()) {
            if (plugin.getConfig().getStringList("cultivar.disabled-worlds").contains(crop.location.getWorld().getName())) continue;

            // Skip dead plants (wither roses)
            if (crop.deathReason != null) continue;

            // Check dying
            if (crop.flags.contains(CropFlag.DYING)) {
                if (now - crop.lastWatered > plugin.getConfig().getLong("cultivar.cannabis.dying-window-minutes", 20) * 60000) {
                    // Die
                    crop.deathReason = "Died from stress";
                    crop.location.getBlock().setType(org.bukkit.Material.WITHER_ROSE);
                    try {
                        cropManager.save(crop);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }

            // Stage advance conditions
            boolean canAdvance = checkAdvanceConditions(crop, now);
            if (canAdvance) {
                advanceStage(crop);
            } else {
                // Log if ready but failing conditions
                long stageTime = crop.getStageTimeMs(plugin.getConfig());
                long timeToAdvance = (crop.stageAdvancedAt + stageTime) - now;
                if (timeToAdvance <= 0) {
                    String reason = "";
                    // Check water
                    long waterExpiry = plugin.getConfig().getLong("cultivar." + crop.type.name().toLowerCase() + ".water-expiry-minutes", 45) * 60000;
                    boolean watered = now - crop.lastWatered <= waterExpiry;
                    World world = crop.location.getWorld();
                    boolean rained = world.hasStorm() && world.getHighestBlockYAt(crop.location) <= crop.location.getBlockY();
                    if (!watered && !rained) reason += "water ";
                    // Check light
                    int light = crop.location.getBlock().getLightLevel();
                    int minLight = switch (crop.type) {
                        case CANNABIS -> 10;
                        case TOBACCO -> crop.flags.contains(CropFlag.LOW_LIGHT) ? 0 : 12;
                        case TEA -> 7;
                    };
                    int maxLight = crop.type == CropType.TEA ? 13 : 15;
                    if (light < minLight || light > maxLight) reason += "light ";
                    if (crop.flags.contains(CropFlag.DYING)) reason += "dying ";
                    if (!reason.isEmpty()) {
                        plugin.getLogger().info("Crop at " + crop.location + " is [Ready] but blocked by: " + reason.trim());
                    }
                }
            }

            // Stress evaluation
            evaluateStress(crop);

            // Set flags
            setAttentionFlags(crop);
        }
    }

    private boolean checkAdvanceConditions(CropRecord crop, long now) {
        // Watered
        long waterExpiry = plugin.getConfig().getLong("cultivar." + crop.type.name().toLowerCase() + ".water-expiry-minutes", 45) * 60000;
        boolean watered = now - crop.lastWatered <= waterExpiry;
        // Or rained
        World world = crop.location.getWorld();
        boolean rained = world.hasStorm() && world.getHighestBlockYAt(crop.location) <= crop.location.getBlockY();

        if (!watered && !rained) return false;

        // Light
        int light = crop.location.getBlock().getLightLevel();
        int minLight = switch (crop.type) {
            case CANNABIS -> 10;
            case TOBACCO -> crop.flags.contains(CropFlag.LOW_LIGHT) ? 0 : 12; // Allow low if flagged
            case TEA -> 7;
        };
        int maxLight = crop.type == CropType.TEA ? 13 : 15;
        if (light < minLight || light > maxLight) {
            if (crop.type == CropType.TOBACCO && light < 12) {
                crop.flags.add(CropFlag.LOW_LIGHT);
            }
            return false;
        }

        // Not dying
        if (crop.flags.contains(CropFlag.DYING)) return false;

        // Time
        long stageTime = crop.getStageTimeMs(plugin.getConfig());

        if (now - crop.stageAdvancedAt >= stageTime) {
            // Overgrow check
            if (crop.stage >= crop.type.getMaxStage()) {
                crop.flags.add(CropFlag.OVERGROWN);
                crop.stress++;
                crop.stageAdvancedAt = now; // Restart timer for next overgrow "tick"
                crop.dirty = true;
                return false; // Don't "advance" stage number, just apply overgrow penalties
            }
            return true;
        }
        return false;
    }

    private void advanceStage(CropRecord crop) {
        crop.stage++;
        crop.stageAdvancedAt = System.currentTimeMillis();
        crop.dirty = true;
        // Update block
        Material newBlock = getVisualBlock(crop);
        crop.location.getBlock().setType(newBlock);

        // Set flags for attention
        if (crop.type == CropType.CANNABIS && (crop.stage == 3 || crop.stage == 4)) {
            crop.flags.add(CropFlag.NEEDS_PRUNING);
        } else if (crop.type == CropType.TOBACCO && (crop.stage == 2 || crop.stage == 4 || crop.stage == 5)) {
            crop.flags.add(CropFlag.NEEDS_STRIPPING);
        }
    }

    private void evaluateStress(CropRecord crop) {
        int stress = 0;

        // Light stress for cannabis
        if (crop.type == CropType.CANNABIS) {
            long time = crop.location.getWorld().getTime();
            if (time >= 6000 && time <= 8000) { // midday
                int light = crop.location.getBlock().getLightLevel();
                if (light > 13) stress++;
            }
        }

        // Overcrowding for cannabis
        if (crop.type == CropType.CANNABIS) {
            int radius = plugin.getConfig().getInt("cultivar.cannabis.overcrowding-radius", 1);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && z == 0) continue;
                    CropRecord other = cropManager.getByLocation(crop.location.clone().add(x, 0, z));
                    if (other != null && other.type == CropType.CANNABIS) {
                        stress++;
                        break;
                    }
                }
            }
        }

        crop.stress += stress;
        if (crop.stress >= 5) {
            crop.flags.add(CropFlag.DYING);
        }
        if (stress > 0) crop.dirty = true;
    }

    private void setAttentionFlags(CropRecord crop) {
        // Tea misting
        if (crop.type == CropType.TEA) {
            long mistInterval = plugin.getConfig().getLong("cultivar.tea.mist-interval-minutes", 30) * 60000;
            if (System.currentTimeMillis() - crop.lastMisted >= mistInterval) {
                crop.flags.add(CropFlag.NEEDS_MISTING);
                crop.dirty = true;
            }
            // Consecutive misses
            if (System.currentTimeMillis() - crop.lastMisted >= mistInterval * 2) {
                crop.flags.add(CropFlag.WILTING);
            }
            if (System.currentTimeMillis() - crop.lastMisted >= mistInterval * 3) {
                // Die
                crop.deathReason = "Died from lack of misting";
                crop.location.getBlock().setType(org.bukkit.Material.WITHER_ROSE);
                try {
                    cropManager.save(crop);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Material getVisualBlock(CropRecord crop) {
        return crop.type.getVisualBlock(crop.stage);
    }
}