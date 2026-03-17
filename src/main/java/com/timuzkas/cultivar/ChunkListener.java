package com.timuzkas.cultivar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {
    private final CropManager cropManager;

    public ChunkListener(CropManager cropManager) {
        this.cropManager = cropManager;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // Could check for heat bonus on load, but for now, maybe not needed
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        // Save dirty crops in this chunk
        for (CropRecord crop : cropManager.getAllInChunk(event.getChunk())) {
            if (crop.dirty) {
                try {
                    cropManager.saveAll(); // Or save individually, but since DB is single connection, ok
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}