package com.timuzkas.cultivar;

import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PipeManager {
    public final Map<UUID, Long> lastSmoked = new HashMap<>();
    public final Map<UUID, BukkitTask> smokeTasks = new HashMap<>();
    public final Map<UUID, Long> pipeEquippedAt = new HashMap<>();

    public void onPipeUnequipped(UUID playerId) {
        BukkitTask task = smokeTasks.remove(playerId);
        if (task != null) task.cancel();
        pipeEquippedAt.remove(playerId);
    }

    public void onPipeLit(UUID playerId, long time) {
        pipeEquippedAt.put(playerId, time);
    }
}