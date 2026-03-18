package com.timuzkas.cultivar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final PlayerStrainManager strainManager;
    private final GrowerManager growerManager;

    public PlayerJoinListener(PlayerStrainManager strainManager, GrowerManager growerManager) {
        this.strainManager = strainManager;
        this.growerManager = growerManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        strainManager.loadPlayerStrains(event.getPlayer().getUniqueId());
        growerManager.loadPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }
}
