package com.timuzkas.cultivar;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class TeaDryingListener implements Listener {

    private final Plugin plugin;
    private final ActionBarAnimator animator;
    private final Map<Location, Integer> dryingCampfires = new HashMap<>();

    public TeaDryingListener(Plugin plugin, ActionBarAnimator animator) {
        this.plugin = plugin;
        this.animator = animator;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (block == null || item == null) {
            return;
        }

        if (
            (block.getType() != Material.CAMPFIRE &&
                block.getType() != Material.SOUL_CAMPFIRE) ||
            !ItemFactory.isFreshTeaLeaf(item)
        ) {
            return;
        }

        if (
            !(block.getBlockData() instanceof Campfire campfire) ||
            !campfire.isLit()
        ) {
            animator.reveal(player, "§cCampfire must be lit", null);
            event.setCancelled(true);
            return;
        }

        Location location = block.getLocation();
        int current = dryingCampfires.getOrDefault(location, 0);

        if (current >= 4) {
            animator.reveal(player, "§cCampfire is full", null);
            event.setCancelled(true);
            return;
        }

        item.setAmount(item.getAmount() - 1);
        dryingCampfires.put(location, current + 1);

        player
            .getWorld()
            .playSound(
                block.getLocation(),
                Sound.BLOCK_CAMPFIRE_CRACKLE,
                1.0f,
                1.0f
            );
        animator.reveal(
            player,
            "§6Drying tea leaf... §7(" + (current + 1) + "/4)",
            null
        );
        event.setCancelled(true);

        plugin
            .getServer()
            .getScheduler()
            .runTaskLater(
                plugin,
                () -> {
                    int left = dryingCampfires.getOrDefault(location, 0);

                    if (left <= 1) {
                        dryingCampfires.remove(location);
                    } else {
                        dryingCampfires.put(location, left - 1);
                    }

                    Block currentBlock = location.getBlock();
                    if (
                        (currentBlock.getType() != Material.CAMPFIRE &&
                            currentBlock.getType() != Material.SOUL_CAMPFIRE) ||
                        !(currentBlock.getBlockData() instanceof
                                Campfire currentCampfire) ||
                        !currentCampfire.isLit()
                    ) {
                        currentBlock
                            .getWorld()
                            .dropItemNaturally(
                                location.clone().add(0.5, 0.5, 0.5),
                                ItemFactory.createFreshTeaLeaf()
                            );
                        return;
                    }

                    currentBlock
                        .getWorld()
                        .dropItemNaturally(
                            location.clone().add(0.5, 1.0, 0.5),
                            ItemFactory.createDriedTeaLeaf()
                        );
                    currentBlock
                        .getWorld()
                        .playSound(
                            location,
                            Sound.ENTITY_ITEM_PICKUP,
                            1.0f,
                            0.8f
                        );
                },
                20L * plugin.getConfig().getInt("cultivar.tea-drying.drying-seconds", 60)
            );
    }
}
