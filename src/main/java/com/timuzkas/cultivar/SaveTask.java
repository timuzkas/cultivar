package com.timuzkas.cultivar;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SaveTask extends BukkitRunnable {
    private final CropManager cropManager;

    public SaveTask(CropManager cropManager) {
        this.cropManager = cropManager;
    }

    @Override
    public void run() {
        try {
            cropManager.saveAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}