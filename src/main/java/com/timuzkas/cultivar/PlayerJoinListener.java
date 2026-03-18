package com.timuzkas.cultivar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final PlayerStrainManager strainManager;

    public PlayerJoinListener(PlayerStrainManager strainManager) {
        this.strainManager = strainManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        strainManager.loadPlayerStrains(event.getPlayer().getUniqueId());
    }
}
