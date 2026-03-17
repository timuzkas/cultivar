package com.timuzkas.cultivar;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProximityNotifyTask extends BukkitRunnable {
    private final CropManager cropManager;
    private final ActionBarAnimator animator;
    private final Plugin plugin;
    private final Map<UUID, Map<String, Long>> lastNotified = new HashMap<>();
    private final long cooldown = 180000; // 3 min

    public ProximityNotifyTask(CropManager cropManager, ActionBarAnimator animator, Plugin plugin) {
        this.cropManager = cropManager;
        this.animator = animator;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            for (CropRecord crop : cropManager.getAll()) {
                if (crop.location.distance(player.getLocation()) > plugin.getConfig().getInt("notifications.proximity-radius", 4)) continue;
                if (!hasAttentionFlag(crop)) continue;

                String key = crop.id;
                long now = System.currentTimeMillis();
                lastNotified.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
                Long last = lastNotified.get(player.getUniqueId()).get(key);
                if (last == null || now - last >= cooldown) {
                    animator.reveal(player, "§e⚠ A plant nearby needs attention", null);
                    lastNotified.get(player.getUniqueId()).put(key, now);
                }
                break; // Only notify once per player per run
            }
        }
    }

    private boolean hasAttentionFlag(CropRecord crop) {
        return crop.flags.contains(CropFlag.NEEDS_PRUNING) ||
               crop.flags.contains(CropFlag.NEEDS_STRIPPING) ||
               crop.flags.contains(CropFlag.NEEDS_MISTING) ||
               crop.flags.contains(CropFlag.DYING) ||
               crop.flags.contains(CropFlag.WILTING);
    }
}