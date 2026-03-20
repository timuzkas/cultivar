package com.timuzkas.cultivar;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CropGrowthTask extends BukkitRunnable {
    private final CropManager cropManager;
    private final Plugin plugin;
    private SoilManager soilManager;
    private GrowerManager growerManager;

    public CropGrowthTask(CropManager cropManager, Plugin plugin) {
        this.cropManager = cropManager;
        this.plugin = plugin;
    }

    public void setSoilManager(SoilManager soilManager) {
        this.soilManager = soilManager;
    }

    public void setGrowerManager(GrowerManager growerManager) {
        this.growerManager = growerManager;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        for (CropRecord crop : cropManager.getAll()) {
            if (plugin.getConfig().getStringList("cultivar.disabled-worlds").contains(crop.location.getWorld().getName())) continue;

            if (crop.deathReason != null) continue;

            World world = crop.location.getWorld();
            boolean isRaining = world.hasStorm() && world.getHighestBlockYAt(crop.location) <= crop.location.getBlockY();
            boolean isThundering = world.isThundering() && isRaining;

            if (isRaining) {
                crop.lastRainedAt = now;
            }

            if (isThundering && crop.type == CropType.CANNABIS && crop.stage <= 1) {
                crop.stress += 1;
                crop.flags.add(CropFlag.STUNTED);
                crop.dirty = true;
                world.spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, crop.location.clone().add(0.5, 1, 0.5), 3, 0.2, 0.2, 0.2, 0);
            }

            if (crop.flags.contains(CropFlag.DYING)) {
                long waterExpiry = plugin.getConfig().getLong("cultivar." + crop.type.name().toLowerCase() + ".water-expiry-minutes", 45) * 60000;
                boolean unwatered = now - crop.lastWatered > waterExpiry;
                boolean drought = crop.lastRainedAt > 0 && (now - crop.lastRainedAt) > (waterExpiry * 2) && unwatered;
                
                if (drought && !crop.flags.contains(CropFlag.WILTING)) {
                    crop.flags.add(CropFlag.WILTING);
                    crop.dirty = true;
                }

                if (now - crop.lastWatered > plugin.getConfig().getLong("cultivar.cannabis.dying-window-minutes", 20) * 60000) {
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

            boolean canAdvance = checkAdvanceConditions(crop, now);
            if (canAdvance) {
                advanceStage(crop);
            } else {
                int enrichment = getSoilEnrichment(crop);
                long stageTime = crop.getStageTimeMs(plugin.getConfig(), enrichment);
                long timeToAdvance = (crop.stageAdvancedAt + stageTime) - now;
                if (timeToAdvance <= 0) {
                    String reason = "";
                    long waterExpiry = plugin.getConfig().getLong("cultivar." + crop.type.name().toLowerCase() + ".water-expiry-minutes", 45) * 60000;
                    boolean watered = now - crop.lastWatered <= waterExpiry;
                    boolean rained = world.hasStorm() && world.getHighestBlockYAt(crop.location) <= crop.location.getBlockY();
                    if (!watered && !rained) reason += "water ";
                    int light = crop.location.getBlock().getLightLevel();
                    int minLight = switch (crop.type) {
                        case CANNABIS -> 10;
                        case TOBACCO -> crop.flags.contains(CropFlag.LOW_LIGHT) ? 0 : 12;
                        case TEA -> 7;
                        case MUSHROOM -> 0;
                    };
                    int maxLight = switch (crop.type) {
                        case TEA -> 13;
                        default -> 15;
                    };
                    if (light < minLight || light > maxLight) reason += "light ";
                    if (crop.flags.contains(CropFlag.DYING)) reason += "dying ";
                    if (!reason.isEmpty()) {
                        plugin.getLogger().info("Crop at " + crop.location + " is [Ready] but blocked by: " + reason.trim());
                    }
                }
            }

            evaluateStress(crop);

            setAttentionFlags(crop);
        }
    }

    private boolean checkAdvanceConditions(CropRecord crop, long now) {
        if (crop.type == CropType.MUSHROOM) {
            int light = crop.location.getBlock().getLightLevel();
            if (light > 7) return false;
            
            Material blockBelow = crop.location.clone().subtract(0, 1, 0).getBlock().getType();
            if (blockBelow != Material.MYCELIUM && blockBelow != Material.PODZOL) return false;
        } else {
            long waterExpiry = plugin.getConfig().getLong("cultivar." + crop.type.name().toLowerCase() + ".water-expiry-minutes", 45) * 60000;
            boolean watered = now - crop.lastWatered <= waterExpiry;
            World world = crop.location.getWorld();
            boolean rained = world.hasStorm() && world.getHighestBlockYAt(crop.location) <= crop.location.getBlockY();

            if (!watered && !rained) return false;

            int light = crop.location.getBlock().getLightLevel();
            int minLight = switch (crop.type) {
                case CANNABIS -> 10;
                case TOBACCO -> crop.flags.contains(CropFlag.LOW_LIGHT) ? 0 : 12;
                case TEA -> 7;
                case MUSHROOM -> 0;
            };
            int maxLight = switch (crop.type) {
                case TEA -> 13;
                default -> 15;
            };
            if (light < minLight || light > maxLight) {
                if (crop.type == CropType.TOBACCO && light < 12) {
                    crop.flags.add(CropFlag.LOW_LIGHT);
                }
                return false;
            }
        }

        if (crop.flags.contains(CropFlag.DYING)) return false;

        int enrichment = getSoilEnrichment(crop);
        long stageTime = crop.getStageTimeMs(plugin.getConfig(), enrichment);

        if (now - crop.stageAdvancedAt >= stageTime) {
            if (crop.stage >= crop.type.getMaxStage()) {
                crop.flags.add(CropFlag.OVERGROWN);
                crop.stress++;
                crop.stageAdvancedAt = now;
                crop.dirty = true;
                return false;
            }
            return true;
        }
        return false;
    }

    private void advanceStage(CropRecord crop) {
        crop.stage++;
        crop.stageAdvancedAt = System.currentTimeMillis();
        crop.dirty = true;
        Material newBlock = getVisualBlock(crop);
        crop.location.getBlock().setType(newBlock);

        if (crop.type == CropType.CANNABIS && (crop.stage == 3 || crop.stage == 4)) {
            crop.flags.add(CropFlag.NEEDS_PRUNING);
        } else if (crop.type == CropType.TOBACCO && (crop.stage == 2 || crop.stage == 4 || crop.stage == 5)) {
            crop.flags.add(CropFlag.NEEDS_STRIPPING);
        }
    }

    private void evaluateStress(CropRecord crop) {
        long now = System.currentTimeMillis();
        long stressCooldown = plugin.getConfig().getLong("cultivar.stress-evaluation-cooldown-minutes", 5) * 60000;
        if (now - crop.lastStressCheck < stressCooldown) {
            return;
        }
        crop.lastStressCheck = now;

        int stress = 0;

        if (crop.type == CropType.CANNABIS) {
            long time = crop.location.getWorld().getTime();
            if (time >= 6000 && time <= 8000) { // midday
                int light = crop.location.getBlock().getLightLevel();
                if (light > 13) stress++;
            }
        }

        if (crop.type == CropType.MUSHROOM) {
            int light = crop.location.getBlock().getLightLevel();
            int lightThreshold = 7;
            if (crop.strainId != null) {
                StrainProfile strain = StrainProfile.generate(crop.strainId, CropType.MUSHROOM);
                lightThreshold += strain.lightTolerance;
            }
            if (light > lightThreshold) {
                stress++;
            }
        }

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
        int dyingThreshold = 5;
        if (crop.strainId != null) {
            StrainProfile strain = StrainProfile.generate(crop.strainId);
            dyingThreshold += strain.stressResistance;
        }
        if (growerManager != null) {
            dyingThreshold += growerManager.getStressBonus(crop.ownerUuid);
        }
        if (crop.stress >= dyingThreshold) {
            crop.flags.add(CropFlag.DYING);
        }
        if (stress > 0) crop.dirty = true;
    }

    private void setAttentionFlags(CropRecord crop) {
        if (crop.type == CropType.TEA) {
            long mistInterval = plugin.getConfig().getLong("cultivar.tea.mist-interval-minutes", 30) * 60000;
            if (System.currentTimeMillis() - crop.lastMisted >= mistInterval) {
                crop.flags.add(CropFlag.NEEDS_MISTING);
                crop.dirty = true;
            }
            if (System.currentTimeMillis() - crop.lastMisted >= mistInterval * 2) {
                crop.flags.add(CropFlag.WILTING);
            }
            if (System.currentTimeMillis() - crop.lastMisted >= mistInterval * 3) {
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

    private int getSoilEnrichment(CropRecord crop) {
        if (soilManager == null) return 0;
        Location farmlandLoc = crop.location.clone().subtract(0, 1, 0);
        return soilManager.getEnrichment(farmlandLoc);
    }
}