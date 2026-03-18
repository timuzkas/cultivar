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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PipeListener implements Listener {

    private final PipeManager pipeManager;
    private final ActionBarAnimator animator;
    private final Plugin plugin;

    public PipeListener(
        PipeManager pipeManager,
        ActionBarAnimator animator,
        Plugin plugin
    ) {
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
        if (
            sneaking &&
            (event.getAction() == Action.RIGHT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
            ItemFactory.isBlankPipe(main)
        ) {
            handleFillPipe(player);
            event.setCancelled(true);
            return;
        }

        // Lighting pipe
        if (!sneaking && main != null && ItemFactory.isFilledPipe(main)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block block = event.getClickedBlock();
                if (
                    block != null &&
                    (block.getType() == Material.CAMPFIRE ||
                        block.getType() == Material.SOUL_CAMPFIRE ||
                        block.getType() == Material.CANDLE ||
                        block.getType() == Material.WHITE_CANDLE ||
                        block.getType() == Material.ORANGE_CANDLE ||
                        block.getType() == Material.MAGENTA_CANDLE ||
                        block.getType() == Material.LIGHT_BLUE_CANDLE ||
                        block.getType() == Material.YELLOW_CANDLE ||
                        block.getType() == Material.LIME_CANDLE ||
                        block.getType() == Material.PINK_CANDLE ||
                        block.getType() == Material.GRAY_CANDLE ||
                        block.getType() == Material.LIGHT_GRAY_CANDLE ||
                        block.getType() == Material.CYAN_CANDLE ||
                        block.getType() == Material.PURPLE_CANDLE ||
                        block.getType() == Material.BLUE_CANDLE ||
                        block.getType() == Material.BROWN_CANDLE ||
                        block.getType() == Material.GREEN_CANDLE ||
                        block.getType() == Material.RED_CANDLE ||
                        block.getType() == Material.BLACK_CANDLE)
                ) {
                    handleLightPipe(player, main);
                    event.setCancelled(true);
                    return;
                }
            }

            if (
                event.getAction() == Action.RIGHT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_BLOCK
            ) {
                ItemStack off = player.getInventory().getItemInOffHand();
                if (off != null && off.getType() == Material.FLINT_AND_STEEL) {
                    handleLightPipe(player, main);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Smoking pipe
        if (
            !sneaking &&
            (event.getAction() == Action.RIGHT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
            ItemFactory.isLitPipe(main)
        ) {
            handleSmokePipe(player, main);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        ItemStack oldItem = player
            .getInventory()
            .getItem(event.getPreviousSlot());

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
        PipeTier tier = ItemFactory.getPipeTier(main);
        String cureType = null;
        boolean isTrim = false;
        boolean isSpliff = false;
        boolean isHerbalFill = false;

        if (ItemFactory.isCannabisBud(off)) {
            material = CropType.CANNABIS;
        } else if (ItemFactory.isCannabisTrim(off)) {
            material = CropType.CANNABIS;
            isTrim = true;
        } else if (ItemFactory.isDryTobaccoLeaf(off)) {
            material = CropType.TOBACCO;
            cureType = FurnaceListener.getCureType(off);
        } else if (ItemFactory.isAirCuredLeaf(off)) {
            material = CropType.TOBACCO;
            cureType = "air";
        } else if (ItemFactory.isSpliff(off)) {
            isSpliff = true;
        } else if (ItemFactory.isHerbalFill(off)) {
            isHerbalFill = true;
        }

        if (material == null && !isSpliff && !isHerbalFill) {
            animator.reveal(
                player,
                "§cNeed cannabis, trim, tobacco, spliff, or herbal fill in offhand",
                null
            );
            return;
        }

        off.setAmount(off.getAmount() - 1);

        ItemStack filled;
        if (isSpliff) {
            String strainId = ItemFactory.getStrainId(off);
            String strainName = ItemFactory.getStrainName(off);
            filled = ItemFactory.createFilledPipe(CropType.CANNABIS, tier);
            ItemMeta meta = filled.getItemMeta();
            if (meta != null) {
                String displayName = tier.getColor() + "⌐ " + tier.name() + " Pipe §8[Spliff]";
                if (strainName != null) {
                    displayName = tier.getColor() + "⌐ " + tier.name() + " Pipe §8[Spliff: " + strainName + "]";
                }
                meta.setDisplayName(displayName);
                meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey("cultivar", "is_spliff"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN,
                    true
                );
                if (strainId != null) {
                    meta.getPersistentDataContainer().set(
                        new org.bukkit.NamespacedKey("cultivar", "strain_id"),
                        org.bukkit.persistence.PersistentDataType.STRING,
                        strainId
                    );
                }
                if (strainName != null) {
                    meta.getPersistentDataContainer().set(
                        new org.bukkit.NamespacedKey("cultivar", "strain_name"),
                        org.bukkit.persistence.PersistentDataType.STRING,
                        strainName
                    );
                }
                filled.setItemMeta(meta);
            }
        } else if (isHerbalFill) {
            filled = ItemFactory.createFilledPipe(CropType.CANNABIS, tier);
            ItemMeta meta = filled.getItemMeta();
            if (meta != null) {
                String displayName = tier.getColor() + "⌐ " + tier.name() + " Pipe §8[Herbal]";
                meta.setDisplayName(displayName);
                meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey("cultivar", "is_herbal"),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN,
                    true
                );
                filled.setItemMeta(meta);
            }
        } else if (isTrim) {
            filled = ItemFactory.createFilledPipe(CropType.CANNABIS, tier);
            ItemMeta meta = filled.getItemMeta();
            if (meta != null) {
                String strainId = ItemFactory.getStrainId(off);
                String strainName = ItemFactory.getStrainName(off);
                String displayName = strainName != null
                    ? tier.getColor() + "⌐ " + tier.name() + " Pipe §8[Trim: " + strainName + "]"
                    : tier.getColor() + "⌐ " + tier.name() + " Pipe §8[Trim]";
                meta.setDisplayName(displayName);
                if (strainId != null) {
                    meta.getPersistentDataContainer().set(
                        new org.bukkit.NamespacedKey("cultivar", "strain_id"),
                        org.bukkit.persistence.PersistentDataType.STRING,
                        strainId
                    );
                }
                if (strainName != null) {
                    meta.getPersistentDataContainer().set(
                        new org.bukkit.NamespacedKey("cultivar", "strain_name"),
                        org.bukkit.persistence.PersistentDataType.STRING,
                        strainName
                    );
                }
                filled.setItemMeta(meta);
            }
        } else {
            filled = ItemFactory.createFilledPipe(material, tier);
        }

        if (cureType != null && material == CropType.TOBACCO) {
            ItemMeta meta = filled.getItemMeta();
            if (meta != null) {
                meta
                    .getPersistentDataContainer()
                    .set(
                        new org.bukkit.NamespacedKey(
                            "cultivar",
                            "tobacco_cure"
                        ),
                        org.bukkit.persistence.PersistentDataType.STRING,
                        cureType
                    );
                String displayName =
                    filled.getItemMeta().getDisplayName() +
                    " §8[" +
                    cureType +
                    "]";
                meta.setDisplayName(displayName);
                filled.setItemMeta(meta);
            }
        }

        player.getInventory().setItemInMainHand(filled);

        player
            .getWorld()
            .playSound(
                player.getLocation(),
                Sound.BLOCK_GRAVEL_PLACE,
                1.4f,
                0.4f
            );

        String matName = isTrim ? "trim" : material.name().toLowerCase();
        animator.reveal(
            player,
            "§8⌐ Pipe packed with " + matName + " — light it with Flint & Steel",
            null
        );
    }

    private void handleLightPipe(Player player, ItemStack filled) {
        CropType material = ItemFactory.getPipeMaterial(filled);
        PipeTier tier = ItemFactory.getPipeTier(filled);
        int smokesUsed = ItemFactory.getPipeSmokesUsed(filled);

        int uses = tier.getMaxUses();
        ItemStack lit = ItemFactory.createLitPipe(
            material,
            uses,
            tier,
            smokesUsed
        );
        player.getInventory().setItemInMainHand(lit);

        player
            .getWorld()
            .playSound(
                player.getLocation(),
                Sound.ITEM_FLINTANDSTEEL_USE,
                1.2f,
                0.5f
            );
        player
            .getWorld()
            .playSound(
                player.getLocation(),
                Sound.BLOCK_FIRE_AMBIENT,
                1.5f,
                0.3f
            );

        SmokeTask task = new SmokeTask(player);
        pipeManager.smokeTasks.put(
            player.getUniqueId(),
            task.runTaskTimer(plugin, 0, 80)
        );

        pipeManager.onPipeLit(player.getUniqueId(), System.currentTimeMillis());

        animator.reveal(
            player,
            "§e⌐ " + tier.name().toLowerCase() + " pipe lit",
            null
        );
    }

    private void handleSmokePipe(Player player, ItemStack lit) {
        long now = System.currentTimeMillis();

        handlePuff(player, lit);

        long cooldown = plugin.getConfig().getLong("cultivar.smoking.cooldown-seconds", 5) * 1000;
        Long last = pipeManager.lastSmoked.get(player.getUniqueId());
        if (last != null && now - last < cooldown) {
            long remaining = (cooldown - (now - last)) / 1000;
            if (remaining > 0) {
                animator.reveal(player, "§7Still feeling the effects... (" + remaining + "s)", null);
            }
            return;
        }

        CropType material = ItemFactory.getPipeMaterial(lit);
        PipeTier tier = ItemFactory.getPipeTier(lit);
        int smokesUsed = ItemFactory.getPipeSmokesUsed(lit);
        boolean isSeasoned = ItemFactory.isPipeSeasoned(lit);
        boolean isTrim = isTrimPipe(lit);
        boolean isSpliff = isSpliffPipe(lit);
        boolean isHerbalFill = isHerbalPipe(lit);

        double durationMultiplier = 1.0;
        if (!isSeasoned && smokesUsed < 3) {
            durationMultiplier = 0.8;
        }
        if (tier == PipeTier.MEERSCHAUM) {
            durationMultiplier *= 1.1;
        }
        if (isTrim) {
            durationMultiplier *= 0.5;
        }
        if (isSpliff) {
            durationMultiplier *= 0.7;
        }

        String materialName = material != null ? material.name().toLowerCase(java.util.Locale.ROOT) : "cannabis";
        
        if (isTrim) {
            String strainId = getTrimStrainId(lit);
            String strainName = getTrimStrainName(lit);
            
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int)(60 * durationMultiplier), 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int)(80 * durationMultiplier), 0));
            
            String msg = "§e⌐ Trim — §7Mild effect";
            if (strainName != null) {
                msg = "§e⌐ " + strainName + " trim — §7Mild effect";
            }
            animator.reveal(player, msg, null);
        } else if (isSpliff) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int)(60 * durationMultiplier), 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int)(100 * durationMultiplier * 0.5), 0));
            
            animator.reveal(player, "§e⌐ Spliff — §7Mixed blend", null);
        } else if (isHerbalFill) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, (int)(120 * 20), 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, (int)(90 * 20), 0));
            
            animator.reveal(player, "§e⌐ Herbal — §7Explorer's blend", null);
        } else {
            String path = "cultivar.smoking." + materialName + "-effects";
            List<String> effects = null;
            
            if (plugin.getConfig().isList(path)) {
                effects = plugin.getConfig().getStringList(path);
            } else {
                org.bukkit.configuration.ConfigurationSection effectsSection = plugin.getConfig().getConfigurationSection(path);
                if (effectsSection != null) {
                    effects = effectsSection.getStringList("effects");
                }
            }

            if (effects != null && !effects.isEmpty()) {
                boolean appliedAny = false;
                for (String effectStr : effects) {
                    String[] parts = effectStr.split(":");
                    if (parts.length < 3) continue;
                    
                    String typeName = parts[0].toLowerCase(java.util.Locale.ROOT);
                    PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.minecraft(typeName));
                    if (type == null) {
                        plugin.getLogger().warning("Unknown potion effect type in config: " + typeName);
                        continue;
                    }
                    
                    int amplifier = Integer.parseInt(parts[1]);
                    int duration = (int) (Integer.parseInt(parts[2]) * 20 * durationMultiplier);
                    
                    if (duration > 0) {
                        player.addPotionEffect(new PotionEffect(type, duration, amplifier));
                        appliedAny = true;
                    }
                }
                if (appliedAny) {
                    String msg = "§e⌐ " + materialName + " — §7Drawing deep...";
                    animator.reveal(player, msg, null);
                }
            } else {
                plugin.getLogger().info("No smoking effects found for path: " + path);
            }

            if (material == CropType.TOBACCO) {
                String cureType = getTobaccoCureType(lit);
                if ("light".equals(cureType)) {
                    player.addPotionEffect(
                        new PotionEffect(PotionEffectType.SPEED, 100, 0)
                    );
                } else if ("dark".equals(cureType)) {
                    player.addPotionEffect(
                        new PotionEffect(PotionEffectType.SPEED, 150, 0)
                    );
                    player.addPotionEffect(
                        new PotionEffect(PotionEffectType.CONFUSION, 60, 0)
                    );
                } else if ("fire".equals(cureType)) {
                    player.addPotionEffect(
                        new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 300, 0)
                    );
                }
            }
        }

        pipeManager.lastSmoked.put(player.getUniqueId(), now);
        pipeManager.onPipeLit(player.getUniqueId(), now);

        smokesUsed++;
        if (smokesUsed >= 3 && !isSeasoned) {
            String matName = isTrim ? "trim" : material.name().toLowerCase();
            animator.reveal(
                player,
                "§e⌐ " + matName + " — §aSeasoned!",
                null
            );
        }
    }

    private void handlePuff(Player player, ItemStack lit) {
        CropType material = ItemFactory.getPipeMaterial(lit);
        PipeTier tier = ItemFactory.getPipeTier(lit);
        int uses = ItemFactory.getPipeUses(lit);
        int smokesUsed = ItemFactory.getPipeSmokesUsed(lit);

        // Visuals
        org.bukkit.util.Vector dir = player
            .getEyeLocation()
            .getDirection()
            .normalize();
        Location smokeLoc = player.getEyeLocation().add(dir.multiply(0.5));

        player
            .getWorld()
            .spawnParticle(
                Particle.SMOKE_LARGE,
                smokeLoc,
                8,
                0.1,
                0.1,
                0.1,
                0.05
            );
        if (Math.random() < 0.2) {
            player
                .getWorld()
                .spawnParticle(
                    Particle.FLAME,
                    smokeLoc,
                    2,
                    0.05,
                    0.05,
                    0.05,
                    0.02
                );
        }

        float pitch = 0.8f + (float) (Math.random() * 0.4f);
        player
            .getWorld()
            .playSound(
                player.getLocation(),
                Sound.ENTITY_FOX_SNIFF,
                0.7f,
                pitch
            );

        pitch = 0.9f + (float) (Math.random() * 0.2f);

        player
            .getWorld()
            .playSound(
                player.getLocation(),
                Sound.ENTITY_PLAYER_BREATH,
                0.25f, // low volume (important)
                pitch
            );

        player
            .getWorld()
            .playSound(
                player.getLocation(),
                Sound.BLOCK_FIRE_AMBIENT,
                1.3f,
                0.3f
            );

        // Durability
        uses--;

        if (uses <= 0) {
            ItemStack blank = ItemFactory.createBlankPipe(tier);
            player.getInventory().setItemInMainHand(blank);
            player
                .getWorld()
                .playSound(
                    player.getLocation(),
                    Sound.BLOCK_FIRE_EXTINGUISH,
                    0.8f,
                    0.5f
                );
            pipeManager.onPipeUnequipped(player.getUniqueId());
            animator.reveal(player, "§8⌐ Pipe spent", null);
        } else {
            // Update the item in hand
            ItemStack updated = ItemFactory.createLitPipe(
                material,
                uses,
                tier,
                smokesUsed
            );
            lit.setItemMeta(updated.getItemMeta());

            if (uses % 2 == 0) {
                animator.reveal(
                    player,
                    "§e⌐ Puff — §7" + uses + " draws left",
                    null
                );
            }
        }
    }

    private String getTobaccoCureType(ItemStack lit) {
        if (lit == null || lit.getItemMeta() == null) return null;
        return lit
            .getItemMeta()
            .getPersistentDataContainer()
            .get(
                new org.bukkit.NamespacedKey("cultivar", "tobacco_cure"),
                org.bukkit.persistence.PersistentDataType.STRING
            );
    }

    private boolean isTrimPipe(ItemStack lit) {
        if (lit == null || lit.getItemMeta() == null) return false;
        String displayName = lit.getItemMeta().getDisplayName();
        return displayName != null && displayName.contains("Trim");
    }

    private String getTrimStrainId(ItemStack lit) {
        if (lit == null || lit.getItemMeta() == null) return null;
        return lit
            .getItemMeta()
            .getPersistentDataContainer()
            .get(
                new org.bukkit.NamespacedKey("cultivar", "strain_id"),
                org.bukkit.persistence.PersistentDataType.STRING
            );
    }

    private String getTrimStrainName(ItemStack lit) {
        if (lit == null || lit.getItemMeta() == null) return null;
        return lit
            .getItemMeta()
            .getPersistentDataContainer()
            .get(
                new org.bukkit.NamespacedKey("cultivar", "strain_name"),
                org.bukkit.persistence.PersistentDataType.STRING
            );
    }

    private boolean isSpliffPipe(ItemStack lit) {
        if (lit == null || lit.getItemMeta() == null) return false;
        return lit.getItemMeta().getPersistentDataContainer().has(
            new org.bukkit.NamespacedKey("cultivar", "is_spliff"),
            org.bukkit.persistence.PersistentDataType.BOOLEAN
        );
    }

    private boolean isHerbalPipe(ItemStack lit) {
        if (lit == null || lit.getItemMeta() == null) return false;
        return lit.getItemMeta().getPersistentDataContainer().has(
            new org.bukkit.NamespacedKey("cultivar", "is_herbal"),
            org.bukkit.persistence.PersistentDataType.BOOLEAN
        );
    }

    private static class SmokeTask extends BukkitRunnable {

        private final Player player;

        public SmokeTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            player
                .getWorld()
                .spawnParticle(
                    Particle.SMOKE_NORMAL,
                    player.getLocation().add(0, 1.5, 0),
                    3,
                    0.1,
                    0.1,
                    0.1,
                    0
                );
        }
    }
}
