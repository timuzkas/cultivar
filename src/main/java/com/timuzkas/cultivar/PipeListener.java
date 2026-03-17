package com.timuzkas.cultivar;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.List;
import java.util.Map;

public class PipeListener implements Listener {
    private final PipeManager pipeManager;
    private final ActionBarAnimator animator;
    private final Plugin plugin;

    public PipeListener(PipeManager pipeManager, ActionBarAnimator animator, Plugin plugin) {
        this.pipeManager = pipeManager;
        this.animator = animator;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack main = event.getItem();
        if (main == null) return;

        boolean sneaking = player.isSneaking();

        // Filling pipe
        if (sneaking && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
            ItemFactory.isBlankPipe(main)) {
            handleFillPipe(player);
            event.setCancelled(true);
            return;
        }

        // Lighting pipe
        if (!sneaking && main != null && ItemFactory.isFilledPipe(main)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block block = event.getClickedBlock();
                if (block != null && (block.getType() == Material.CAMPFIRE || block.getType() == Material.SOUL_CAMPFIRE ||
                                      block.getType() == Material.CANDLE || block.getType() == Material.WHITE_CANDLE ||
                                      block.getType() == Material.ORANGE_CANDLE || block.getType() == Material.MAGENTA_CANDLE ||
                                      block.getType() == Material.LIGHT_BLUE_CANDLE || block.getType() == Material.YELLOW_CANDLE ||
                                      block.getType() == Material.LIME_CANDLE || block.getType() == Material.PINK_CANDLE ||
                                      block.getType() == Material.GRAY_CANDLE || block.getType() == Material.LIGHT_GRAY_CANDLE ||
                                      block.getType() == Material.CYAN_CANDLE || block.getType() == Material.PURPLE_CANDLE ||
                                      block.getType() == Material.BLUE_CANDLE || block.getType() == Material.BROWN_CANDLE ||
                                      block.getType() == Material.GREEN_CANDLE || block.getType() == Material.RED_CANDLE ||
                                      block.getType() == Material.BLACK_CANDLE)) {
                    handleLightPipe(player, main);
                    event.setCancelled(true);
                    return;
                }
            } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                ItemStack off = player.getInventory().getItemInOffHand();
                if (off != null && off.getType() == Material.FLINT_AND_STEEL) {
                    handleLightPipe(player, main);
                    // Consume flint durability?
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Smoking pipe
        if (!sneaking && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
            ItemFactory.isLitPipe(main)) {
            handleSmokePipe(player, main);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());

        if (ItemFactory.isLitPipe(oldItem) && !ItemFactory.isLitPipe(newItem)) {
            pipeManager.onPipeUnequipped(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (ItemFactory.isLitPipe(item)) {
            pipeManager.onPipeUnequipped(event.getPlayer().getUniqueId());
        }
    }

    private void handleFillPipe(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();

        CropType material = null;
        if (ItemFactory.isCannabisBud(off)) {
            material = CropType.CANNABIS;
        } else if (ItemFactory.isDryTobaccoLeaf(off)) {
            material = CropType.TOBACCO;
        }

        if (material == null) {
            animator.reveal(player, "§cNeed cannabis or tobacco in offhand", null);
            return;
        }

        // Consume offhand
        off.setAmount(off.getAmount() - 1);

        // Replace main
        ItemStack filled = ItemFactory.createFilledPipe(material);
        player.getInventory().setItemInMainHand(filled);

        // Sound
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRAVEL_PLACE, 1.4f, 0.4f);

        animator.reveal(player, "§8⌐ Pipe packed — light it with Flint & Steel", null);
    }

    private void handleLightPipe(Player player, ItemStack filled) {
        CropType material = ItemFactory.getPipeMaterial(filled);

        // Replace with lit
        int uses = plugin.getConfig().getInt("smoking.pipe-uses", 10);
        ItemStack lit = ItemFactory.createLitPipe(material, uses);
        player.getInventory().setItemInMainHand(lit);

        // Sounds
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1.2f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1.5f, 0.3f);

        // Idle smoke
        SmokeTask task = new SmokeTask(player);
        pipeManager.smokeTasks.put(player.getUniqueId(), task.runTaskTimer(plugin, 0, 80)); // every 4s

        pipeManager.onPipeLit(player.getUniqueId(), System.currentTimeMillis());

        animator.reveal(player, "§e⌐ Pipe lit", null);
    }

    private void handleSmokePipe(Player player, ItemStack lit) {
        long now = System.currentTimeMillis();
        long cooldown = plugin.getConfig().getLong("smoking.cooldown-seconds", 120) * 1000;
        Long last = pipeManager.lastSmoked.get(player.getUniqueId());
        if (last != null && now - last < cooldown) {
            animator.reveal(player, "§7Already smoking", null);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.4f, 0.3f);
            return;
        }

        CropType material = ItemFactory.getPipeMaterial(lit);
        int uses = ItemFactory.getPipeUses(lit);

        // Apply effects
        Map<String, Object> config = plugin.getConfig().getConfigurationSection("smoking." + material.name().toLowerCase() + "-effects").getValues(false);
        @SuppressWarnings("unchecked")
        List<String> effects = (List<String>) config.get("effects");
        for (String effectStr : effects) {
            String[] parts = effectStr.split(":");
            PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.minecraft(parts[0].toLowerCase()));
            if (type == null) continue;
            int amplifier = Integer.parseInt(parts[1]) - 1;
            int duration = Integer.parseInt(parts[2]) * 20;
            player.addPotionEffect(new PotionEffect(type, duration, amplifier));
        }

        // Decrement uses
        uses--;
        if (uses <= 0) {
            // Spent
            ItemStack blank = ItemFactory.createBlankPipe();
            player.getInventory().setItemInMainHand(blank);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 0.5f);
            pipeManager.onPipeUnequipped(player.getUniqueId());
            animator.reveal(player, "§8⌐ Pipe spent", null);
        } else {
            // Update PDC
            lit.setItemMeta(ItemFactory.createLitPipe(material, uses).getItemMeta());
            // Sounds and particles
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 0.6f, 0.5f);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1.3f, 0.3f);
            player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation().add(0, 1.5, 0), 5, 0.1, 0.1, 0.1, 0);
            pipeManager.lastSmoked.put(player.getUniqueId(), now);
            pipeManager.onPipeLit(player.getUniqueId(), now); // reset timeout
            animator.reveal(player, "§e⌐ " + material.name().toLowerCase() + " — §7" + uses + " draws left", null);
        }
    }

    private static class SmokeTask extends BukkitRunnable {
        private final Player player;

        public SmokeTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 1.5, 0), 3, 0.1, 0.1, 0.1, 0);
        }
    }
}