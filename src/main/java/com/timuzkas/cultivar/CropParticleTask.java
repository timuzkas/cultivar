package com.timuzkas.cultivar;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CropParticleTask extends BukkitRunnable {

    private final CropManager cropManager;
    private final Plugin plugin;

    public CropParticleTask(CropManager cropManager, Plugin plugin) {
        this.cropManager = cropManager;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            for (CropRecord crop : cropManager.getAll()) {
                if (crop.location.distance(player.getLocation()) > 20) continue;

                Particle particle = null;
                int count = 1;

                if (
                    crop.flags.contains(CropFlag.NEEDS_PRUNING) ||
                    crop.flags.contains(CropFlag.NEEDS_STRIPPING)
                ) {
                    particle = Particle.VILLAGER_ANGRY;
                    count = 1;
                } else if (crop.flags.contains(CropFlag.NEEDS_MISTING)) {
                    particle = Particle.WATER_SPLASH;
                    count = 5;
                } else if (crop.stress >= 3) {
                    particle = Particle.SMOKE_NORMAL;
                    count = crop.stress >= 4 ? 3 : 1;
                } else if (
                    crop.flags.contains(CropFlag.DYING) ||
                    crop.flags.contains(CropFlag.WILTING)
                ) {
                    particle = Particle.SMOKE_NORMAL;
                    count = 3;
                } else if (
                    crop.stage > 0 && crop.stage < crop.type.getMaxStage()
                ) {
                    // advancing
                    particle = Particle.VILLAGER_HAPPY;
                    count = 2;
                }

                if (particle != null) {
                    player
                        .getWorld()
                        .spawnParticle(
                            particle,
                            crop.location.clone().add(0.5, 0.5, 0.5),
                            count,
                            0.2,
                            0.2,
                            0.2,
                            0
                        );
                }
            }
        }
    }
}
