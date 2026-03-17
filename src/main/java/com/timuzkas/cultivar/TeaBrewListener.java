package com.timuzkas.cultivar;

import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class TeaBrewListener implements Listener {

    private final TeaBrewManager teaBrewManager;
    private final ActionBarAnimator animator;
    private final Plugin plugin;

    public TeaBrewListener(
        TeaBrewManager teaBrewManager,
        ActionBarAnimator animator,
        Plugin plugin
    ) {
        this.teaBrewManager = teaBrewManager;
        this.animator = animator;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (
            event.getAction() != Action.RIGHT_CLICK_BLOCK &&
            event.getAction() != Action.RIGHT_CLICK_AIR
        ) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();

        // Add leaves to cauldron
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            block != null &&
            isTeaCauldron(block) &&
            item != null &&
            ItemFactory.isAnyTeaLeaf(item)
        ) {
            if (!isBrewingSetup(block)) {
                animator.reveal(player, "§cNeeds fire underneath", null);
                event.setCancelled(true);
                return;
            }

            handleAddLeaves(player, block, item);
            event.setCancelled(true);
            return;
        }

        // Collect with teapot
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            block != null &&
            isTeaCauldron(block) &&
            item != null &&
            ItemFactory.isEmptyTeapot(item)
        ) {
            handleCollectTea(player, block, item);
            event.setCancelled(true);
            return;
        }

        // Check brewing status
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            block != null &&
            isTeaCauldron(block)
        ) {
            TeaBrewManager.BrewSession session =
                teaBrewManager.activeSessions.get(block.getLocation());
            if (session != null && !session.complete) {
                handleCheckBrewingStatus(player, session);
                event.setCancelled(true);
                return;
            }
        }

        // Pour cup
        if (
            (event.getAction() == Action.RIGHT_CLICK_BLOCK ||
                event.getAction() == Action.RIGHT_CLICK_AIR) &&
            item != null &&
            ItemFactory.isBrewedTeapot(item)
        ) {
            handlePourCup(player, item);
            event.setCancelled(true);
            return;
        }

        // Drink cup
        if (
            event.getAction() == Action.RIGHT_CLICK_BLOCK ||
            event.getAction() == Action.RIGHT_CLICK_AIR
        ) {
            if (item != null && ItemFactory.isCupOfTea(item)) {
                handleDrinkTea(player, item);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (
            isTeaCauldron(block) ||
            block.getType() == Material.CAMPFIRE ||
            block.getType() == Material.SOUL_CAMPFIRE
        ) {
            TeaBrewManager.BrewSession session =
                teaBrewManager.activeSessions.get(block.getLocation());
            if (session != null) {
                teaBrewManager.activeSessions.remove(block.getLocation());
                notifyNearbyPlayers(
                    block.getLocation(),
                    "§c✖ Brew interrupted"
                );
            }
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();

        if (
            block.getType() == Material.CAMPFIRE ||
            block.getType() == Material.SOUL_CAMPFIRE
        ) {
            if (
                block.getBlockData() instanceof
                    org.bukkit.block.data.type.Campfire campfire &&
                !campfire.isLit()
            ) {
                Block above = block.getRelative(0, 1, 0);
                if (isTeaCauldron(above)) {
                    TeaBrewManager.BrewSession session =
                        teaBrewManager.activeSessions.get(above.getLocation());
                    if (session != null) {
                        teaBrewManager.activeSessions.remove(
                            above.getLocation()
                        );
                        notifyNearbyPlayers(
                            above.getLocation(),
                            "§c✖ Brew interrupted"
                        );
                    }
                }
            }
        }
    }

    private void handleAddLeaves(
        Player player,
        Block cauldron,
        ItemStack leaves
    ) {
        if (teaBrewManager.activeSessions.containsKey(cauldron.getLocation())) {
            animator.reveal(player, "§cAlready brewing", null);
            return;
        }

        if (
            !(cauldron.getBlockData() instanceof
                    org.bukkit.block.data.Levelled levelled)
        ) {
            animator.reveal(player, "§cCauldron needs water", null);
            return;
        }

        int required = plugin
            .getConfig()
            .getInt("cultivar.tea-brewing.leaves-required", 2);
        if (leaves.getAmount() < required) {
            animator.reveal(player, "§cNeed " + required + " leaves", null);
            return;
        }

        boolean isDried = ItemFactory.isDriedTeaLeaf(leaves);
        leaves.setAmount(leaves.getAmount() - required);

        TeaBrewManager.BrewSession session = new TeaBrewManager.BrewSession();
        session.startedAt = System.currentTimeMillis();
        session.variant = isDried
            ? "black"
            : (Math.random() < 0.5 ? "green" : "herbal");
        session.complete = false;
        session.startedBy = player.getUniqueId();
        teaBrewManager.activeSessions.put(cauldron.getLocation(), session);

        long brewTime =
            plugin.getConfig().getLong("cultivar.tea-brewing.brew-seconds", 60) * 1000;
        int seconds = (int) (brewTime / 1000);

        player
            .getWorld()
            .playSound(
                cauldron.getLocation(),
                Sound.BLOCK_BREWING_STAND_BREW,
                0.8f,
                0.6f
            );
        animator.reveal(
            player,
            "§7❋ Brewing " + session.variant + " tea... " + seconds + "s",
            null
        );

        new BrewParticleTask(cauldron.getLocation()).runTaskTimer(
            plugin,
            0,
            100
        );

        plugin
            .getServer()
            .getScheduler()
            .runTaskLater(
                plugin,
                () -> {
                    session.complete = true;
                    player
                        .getWorld()
                        .spawnParticle(
                            Particle.FLAME,
                            cauldron.getLocation().add(0.5, 1, 0.5),
                            12,
                            0.3,
                            0.3,
                            0.3,
                            0
                        );
                },
                brewTime / 50
            );
    }

    private void handleCheckBrewingStatus(
        Player player,
        TeaBrewManager.BrewSession session
    ) {
        long brewTime =
            plugin.getConfig().getLong("cultivar.tea-brewing.brew-seconds", 60) * 1000;
        long elapsed = System.currentTimeMillis() - session.startedAt;
        long remaining = brewTime - elapsed;
        int secondsLeft = (int) Math.ceil(remaining / 1000.0);

        player
            .getWorld()
            .playSound(
                player.getLocation(),
                Sound.ITEM_BUCKET_EMPTY,
                0.5f,
                1.2f
            );
        animator.reveal(player, "§7❋ " + secondsLeft + "s remaining", null);
    }

    private void handleCollectTea(
        Player player,
        Block cauldron,
        ItemStack teapot
    ) {
        TeaBrewManager.BrewSession session = teaBrewManager.activeSessions.get(
            cauldron.getLocation()
        );
        if (session == null || !session.complete) {
            animator.reveal(player, "§cNot ready yet", null);
            return;
        }

        teaBrewManager.activeSessions.remove(cauldron.getLocation());

        int maxCups = plugin
            .getConfig()
            .getInt("cultivar.tea-brewing.max-cups-per-brew", 4);
        ItemStack brewed = ItemFactory.createBrewedTeapot(
            session.variant,
            maxCups
        );
        player.getInventory().setItemInMainHand(brewed);

        if (cauldron.getType() == Material.WATER_CAULDRON) {
            cauldron.setType(Material.CAULDRON);
        }

        player
            .getWorld()
            .playSound(
                cauldron.getLocation(),
                Sound.ITEM_BOTTLE_FILL,
                1.2f,
                0.6f
            );
        player
            .getWorld()
            .playSound(
                cauldron.getLocation(),
                Sound.ENTITY_PLAYER_LEVELUP,
                1.5f,
                0.4f
            );
        player
            .getWorld()
            .spawnParticle(
                Particle.FLAME,
                cauldron.getLocation().add(0.5, 1, 0.5),
                12,
                0.3,
                0.3,
                0.3,
                0
            );

        animator.reveal(player, "§a✦ Tea ready — " + session.variant, null);
    }

    private void handlePourCup(Player player, ItemStack teapot) {
        String variant = ItemFactory.getTeaVariant(teapot);
        int cups = ItemFactory.getTeaCups(teapot);

        if (cups <= 0) {
            animator.reveal(player, "§cTeapot empty", null);
            return;
        }

        ItemStack bottle = null;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.GLASS_BOTTLE) {
                bottle = item;
                break;
            }
        }

        if (bottle == null) {
            animator.reveal(player, "§cNeed empty bottle", null);
            return;
        }

        bottle.setAmount(bottle.getAmount() - 1);

        cups--;
        if (cups == 0) {
            ItemStack empty = ItemFactory.createEmptyTeapot();
            player.getInventory().setItemInMainHand(empty);
        } else {
            teapot.setItemMeta(
                ItemFactory.createBrewedTeapot(variant, cups).getItemMeta()
            );
        }

        ItemStack cup = ItemFactory.createCupOfTea(variant);
        player.getInventory().addItem(cup);

        player
            .getWorld()
            .playSound(
                player.getLocation(),
                Sound.ITEM_BOTTLE_FILL,
                1.4f,
                0.4f
            );

        animator.reveal(player, "§b☕ Poured — §7" + cups + " cups left", null);
    }

    private void handleDrinkTea(Player player, ItemStack cup) {
        String variant = ItemFactory.getTeaVariant(cup);
        if (variant == null) {
            return;
        }
        
        var section = plugin.getConfig().getConfigurationSection("cultivar.tea-brewing.variants." + variant);
        if (section == null) {
            return;
        }
        
        Map<String, Object> config = section.getValues(false);

        @SuppressWarnings("unchecked")
        List<String> effects = (List<String>) config.get("effects");

        for (String effectStr : effects) {
            String[] parts = effectStr.split(":");
            PotionEffectType type = PotionEffectType.getByKey(
                NamespacedKey.minecraft(parts[0].toLowerCase())
            );
            if (type == null) {
                continue;
            }

            int amplifier = Integer.parseInt(parts[1]);
            int duration = Integer.parseInt(parts[2]) * 20;
            player.addPotionEffect(
                new PotionEffect(type, duration, amplifier - 1)
            );
        }

        String message = (String) config.get("message");

        cup.setAmount(cup.getAmount() - 1);

        player
            .getWorld()
            .playSound(
                player.getLocation(),
                Sound.ENTITY_GENERIC_DRINK,
                1.0f,
                1.0f
            );
        player
            .getWorld()
            .playSound(
                player.getLocation(),
                Sound.ENTITY_PLAYER_LEVELUP,
                1.6f,
                0.3f
            );
        player
            .getWorld()
            .spawnParticle(
                Particle.FLAME,
                player.getLocation().add(0, 1, 0),
                6,
                0.3,
                0.3,
                0.3,
                0
            );

        animator.reveal(player, "§b☕ " + message, null);
    }

    private boolean isBrewingSetup(Block cauldron) {
        Block below = cauldron.getRelative(0, -1, 0);
        return (
            below.getType() == Material.CAMPFIRE ||
            below.getType() == Material.SOUL_CAMPFIRE
        );
    }

    private boolean isTeaCauldron(Block block) {
        return (
            block.getType() == Material.CAULDRON ||
            block.getType() == Material.WATER_CAULDRON
        );
    }

    private String getRandomVariant() {
        List<String> variants = List.of("green", "herbal");
        return variants.get((int) (Math.random() * variants.size()));
    }

    private void notifyNearbyPlayers(Location location, String message) {
        location
            .getWorld()
            .getPlayers()
            .stream()
            .filter(player -> player.getLocation().distance(location) <= 10)
            .forEach(player -> animator.reveal(player, message, null));
    }

    private class BrewParticleTask extends BukkitRunnable {

        private final Location location;
        private int tick = 0;

        public BrewParticleTask(Location location) {
            this.location = location;
        }

        @Override
        public void run() {
            TeaBrewManager.BrewSession session =
                teaBrewManager.activeSessions.get(location);
            if (session == null || session.complete) {
                cancel();
                return;
            }

            tick++;
            location
                .getWorld()
                .spawnParticle(
                    Particle.WATER_SPLASH,
                    location.clone().add(0.5, 1, 0.5),
                    5,
                    0.2,
                    0.2,
                    0.2,
                    0
                );
            location
                .getWorld()
                .spawnParticle(
                    Particle.WHITE_ASH,
                    location.clone().add(0.5, 1.2, 0.5),
                    3,
                    0.3,
                    0.3,
                    0.3,
                    0.02
                );

            if (tick % 20 == 0) {
                location
                    .getWorld()
                    .playSound(location, Sound.ITEM_BUCKET_EMPTY, 0.3f, 0.8f);
            }
        }
    }
}
