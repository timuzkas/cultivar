package com.timuzkas.cultivar;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID;

public class PipeMonitorTask extends BukkitRunnable {
    private final PipeManager pipeManager;
    private final Plugin plugin;

    public PipeMonitorTask(PipeManager pipeManager, Plugin plugin) {
        this.pipeManager = pipeManager;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long timeout = plugin.getConfig().getLong("smoking.pipe-timeout-minutes", 5) * 60000;
        long now = System.currentTimeMillis();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            Long equippedAt = pipeManager.pipeEquippedAt.get(uuid);
            if (equippedAt != null && now - equippedAt >= timeout) {
                // Extinguish
                ItemStack main = player.getInventory().getItemInMainHand();
                if (ItemFactory.isLitPipe(main)) {
                    CropType material = ItemFactory.getPipeMaterial(main);
                    ItemStack filled = ItemFactory.createFilledPipe(material);
                    player.getInventory().setItemInMainHand(filled);
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.3f);
                    pipeManager.onPipeUnequipped(uuid);
                    // Action bar
                    ActionBarAnimator animator = ((Cultivar) plugin).getAnimator();
                    animator.reveal(player, "§8⌐ Pipe went out", null);
                }
            }
        }
    }
}