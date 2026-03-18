package com.timuzkas.cultivar;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HarvestBasketManager {
    private final NamespacedKey basketKey;
    private final ActionBarAnimator animator;

    public HarvestBasketManager(Cultivar plugin, ActionBarAnimator animator) {
        this.basketKey = new NamespacedKey(plugin, "basket_contents");
        this.animator = animator;
    }

    public void addToBasket(Player player, ItemStack basket, ItemStack item) {
        ItemMeta meta = basket.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        List<ItemStack> contents = getBasketContents(pdc);
        
        // Try to stack with existing items
        boolean stacked = false;
        for (ItemStack existing : contents) {
            if (existing.isSimilar(item)) {
                existing.setAmount(existing.getAmount() + item.getAmount());
                stacked = true;
                break;
            }
        }
        
        if (!stacked) {
            contents.add(item.clone());
        }

        pdc.set(basketKey, PersistentDataType.STRING, InventorySerializer.serialize(contents));
        basket.setItemMeta(meta);
        
        if (animator != null) {
            animator.reveal(player, "§6+ Harvested to basket", null);
        }
    }

    public void handleDumpBasket(Player player, ItemStack basket) {
        ItemMeta meta = basket.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(basketKey, PersistentDataType.STRING)) {
            if (animator != null) animator.reveal(player, "§eBasket is empty", null);
            return;
        }

        List<ItemStack> contents = getBasketContents(pdc);
        if (contents.isEmpty()) {
            if (animator != null) animator.reveal(player, "§eBasket is empty", null);
            return;
        }

        int dumped = 0;
        List<ItemStack> remainingInBasket = new ArrayList<>();

        for (ItemStack stack : contents) {
            int originalAmount = stack.getAmount();
            java.util.HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(stack);
            
            if (!remaining.isEmpty()) {
                ItemStack leftover = remaining.values().iterator().next();
                dumped += (originalAmount - leftover.getAmount());
                remainingInBasket.add(leftover);
            } else {
                dumped += originalAmount;
            }
        }

        if (dumped > 0) {
            if (remainingInBasket.isEmpty()) {
                pdc.remove(basketKey);
            } else {
                pdc.set(basketKey, PersistentDataType.STRING, InventorySerializer.serialize(remainingInBasket));
            }
            basket.setItemMeta(meta);
            if (animator != null) animator.reveal(player, "§6- Dumped " + dumped + " items", null);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        } else {
            if (animator != null) animator.reveal(player, "§cInventory full!", null);
        }
    }

    private List<ItemStack> getBasketContents(PersistentDataContainer pdc) {
        String data = pdc.get(basketKey, PersistentDataType.STRING);
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // Check if it's the new format (likely Base64) or old format
            if (isBase64(data)) {
                return InventorySerializer.deserialize(data);
            } else {
                // Fallback to old format if possible
                return parseOldFormat(data);
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private boolean isBase64(String data) {
        // Simple heuristic for Base64 vs old format (which used : and |)
        return !data.contains(":") && !data.contains("|");
    }

    private List<ItemStack> parseOldFormat(String contents) {
        List<ItemStack> items = new ArrayList<>();
        String[] entries = contents.split(";");
        for (String itemData : entries) {
            String[] parts = itemData.split(":");
            if (parts.length < 2) continue;

            try {
                String materialName = parts[0];
                String[] amountAndMeta = parts[1].split("\\|", -1);
                int amount = Integer.parseInt(amountAndMeta[0]);
                Material mat = Material.getMaterial(materialName);
                if (mat != null) {
                    ItemStack stack = new ItemStack(mat, amount);
                    if (amountAndMeta.length >= 2) {
                        ItemMeta stackMeta = stack.getItemMeta();
                        String displayName = amountAndMeta[1].replace("\\|", "|");
                        stackMeta.setDisplayName(displayName);
                        if (amountAndMeta.length >= 3) {
                            String lore = amountAndMeta[2].replace("\\|", "|").replace("|||", "\n");
                            List<String> loreList = java.util.Arrays.asList(lore.split("\n"));
                            stackMeta.setLore(loreList);
                        }
                        stack.setItemMeta(stackMeta);
                    }
                    items.add(stack);
                }
            } catch (Exception ignored) {}
        }
        return items;
    }
}
