package com.timuzkas.cultivar;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CultivarCommand implements CommandExecutor, TabCompleter {
    private final CropManager cropManager;
    private final ActionBarAnimator animator;
    private final org.bukkit.plugin.Plugin plugin;

    public CultivarCommand(CropManager cropManager, ActionBarAnimator animator, org.bukkit.plugin.Plugin plugin) {
        this.cropManager = cropManager;
        this.animator = animator;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            // List crops
            List<CropRecord> crops = cropManager.getAll().stream()
                    .filter(c -> c.ownerUuid.equals(player.getUniqueId()))
                    .collect(Collectors.toList());
            if (crops.isEmpty()) {
                animator.reveal(player, "§7No active crops", null);
            } else {
                for (CropRecord crop : crops) {
                    String flags = crop.flags.isEmpty() ? "" : " [" + crop.flags.stream().map(Enum::name).collect(Collectors.joining(", ")) + "]";
                    animator.reveal(player, "§a" + crop.type.name().toLowerCase() + " at " + crop.location.getBlockX() + "," + crop.location.getBlockZ() + " stage " + crop.stage + flags, null);
                }
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "inspect" -> {
                // Look at crop
                CropRecord crop = cropManager.getByLocation(player.getTargetBlockExact(5).getLocation());
                if (crop == null) {
                    animator.reveal(player, "§cNot looking at a crop", null);
                } else {
                    long now = System.currentTimeMillis();
                    long stageTime = crop.getStageTimeMs(plugin.getConfig());
                    long timeToAdvance = (crop.stageAdvancedAt + stageTime) - now;

                    long waterExpiry = plugin.getConfig().getLong("cultivar." + crop.type.name().toLowerCase() + ".water-expiry-minutes", 45) * 60000;
                    boolean watered = now - crop.lastWatered <= waterExpiry;
                    org.bukkit.World world = crop.location.getWorld();
                    boolean rained = world.hasStorm() && world.getHighestBlockYAt(crop.location) <= crop.location.getBlockY();
                    boolean isWatered = watered || rained;
                    int light = crop.location.getBlock().getLightLevel();

                    int minLight = switch (crop.type) {
                        case CANNABIS -> 10;
                        case TOBACCO -> crop.flags.contains(CropFlag.LOW_LIGHT) ? 0 : 12; // Allow low if flagged
                        case TEA -> 7;
                    };
                    int maxLight = crop.type == CropType.TEA ? 13 : 15;
                    boolean lightOk = light >= minLight && light <= maxLight;

                    String wateredStr = isWatered ? "§bWatered" : "§cDry";
                    String lightStr = "§eLight: " + light;
                    String readyStr = (timeToAdvance <= 0) ? " §a[Ready]" : "";

                    String status = "§a" + crop.type.name().toLowerCase() + " stage " + crop.stage + " stress " + crop.stress + " [" + wateredStr + "§a] " + lightStr + readyStr;

                    // Show specific blockers if ready but not advancing
                    if (timeToAdvance <= 0) {
                        if (!isWatered) {
                            status += " §cNeeds Water";
                        }
                        if (!lightOk) {
                            status += " §cLow Light";
                        }
                        if (crop.flags.contains(CropFlag.DYING)) {
                            status += " §cDying";
                        }
                        if (crop.type == CropType.CANNABIS && (crop.stage == 3 || crop.stage == 4) && crop.flags.contains(CropFlag.NEEDS_PRUNING)) {
                            status += " §cPaused: Pruning Needed";
                        }
                        if (crop.type == CropType.TOBACCO && (crop.stage == 2 || crop.stage == 4 || crop.stage == 5) && crop.flags.contains(CropFlag.NEEDS_STRIPPING)) {
                            status += " §cPaused: Stripping Needed";
                        }
                        if (crop.type == CropType.TEA && crop.flags.contains(CropFlag.NEEDS_MISTING)) {
                            status += " §cPaused: Misting Needed";
                        }
                    }

                    if (!crop.flags.isEmpty()) {
                        status += " flags: " + crop.flags.stream().map(Enum::name).collect(Collectors.joining(", "));
                    }
                    if (timeToAdvance > 0) {
                        status += " next advance in " + (timeToAdvance / 1000) + "s";
                    }
                    animator.instant(player, status);
                }
            }
            case "give" -> {
                if (!player.hasPermission("cultivar.admin")) {
                    animator.reveal(player, "§cNo permission", null);
                    return true;
                }
                if (args.length < 3) {
                    animator.reveal(player, "§cUsage: /cv give <player> <item> [amount]", null);
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    animator.reveal(player, "§cPlayer not found", null);
                    return true;
                }
                int amount = args.length >= 4 ? Math.max(1, Integer.parseInt(args[3])) : 1;
                ItemStack item = switch (args[2].toLowerCase()) {
                    case "cannabis_seed" -> ItemFactory.createCannabisSeed();
                    case "tobacco_seed" -> ItemFactory.createTobaccoSeed();
                    case "tea_seed" -> ItemFactory.createTeaSeed();
                    case "tea_leaf" -> ItemFactory.createFreshTeaLeaf();
                    case "dried_tea_leaf" -> ItemFactory.createDriedTeaLeaf();
                    case "compost" -> ItemFactory.createCompost();
                    case "pipe" -> ItemFactory.createBlankPipe();
                    case "teapot" -> ItemFactory.createEmptyTeapot();
                    default -> null;
                };
                if (item == null) {
                    animator.reveal(player, "§cInvalid item", null);
                } else {
                    item.setAmount(amount);
                    target.getInventory().addItem(item);
                    animator.reveal(player, "§aGiven " + amount + " " + args[2] + " to " + target.getName(), null);
                }
            }
            case "remove" -> {
                if (!player.hasPermission("cultivar.admin")) {
                    animator.reveal(player, "§cNo permission", null);
                    return true;
                }
                CropRecord crop = cropManager.getByLocation(player.getTargetBlockExact(5).getLocation());
                if (crop == null) {
                    animator.reveal(player, "§cNot looking at a crop", null);
                } else {
                    try {
                        revertBlock(crop);
                        cropManager.remove(crop.location);
                        animator.reveal(player, "§aCrop removed", null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        animator.reveal(player, "§cError removing crop", null);
                    }
                }
            }
            case "reload" -> {
                if (!player.hasPermission("cultivar.admin")) {
                    animator.reveal(player, "§cNo permission", null);
                    return true;
                }
                plugin.reloadConfig();
                animator.reveal(player, "§aConfig reloaded", null);
            }
            case "force" -> {
                if (!player.hasPermission("cultivar.admin")) {
                    animator.reveal(player, "§cNo permission", null);
                    return true;
                }
                if (args.length < 3) {
                    animator.reveal(player, "§cUsage: /cv force <stage|stress|flag|advance> <value>", null);
                    return true;
                }
                org.bukkit.block.Block targetBlock = player.getTargetBlockExact(5);
                if (targetBlock == null) {
                    animator.reveal(player, "§cNot looking at a block", null);
                    return true;
                }
                CropRecord crop = cropManager.getByLocation(targetBlock.getLocation());
                if (crop == null) {
                    animator.reveal(player, "§cNot looking at a crop", null);
                } else {
                    String sub = args[1].toLowerCase();
                    String val = args[2];
                    switch (sub) {
                        case "stage" -> {
                            int stage = Integer.parseInt(val);
                            crop.stage = stage;
                            crop.stageAdvancedAt = System.currentTimeMillis();
                            crop.dirty = true;
                            crop.location.getBlock().setType(crop.type.getVisualBlock(stage));
                            animator.reveal(player, "§aForced stage to " + stage, null);
                        }
                        case "stress" -> {
                            int stress = Integer.parseInt(val);
                            crop.stress = stress;
                            crop.dirty = true;
                            animator.reveal(player, "§aForced stress to " + stress, null);
                        }
                        case "flag" -> {
                            try {
                                CropFlag flag = CropFlag.valueOf(val.toUpperCase());
                                if (crop.flags.contains(flag)) {
                                    crop.flags.remove(flag);
                                    animator.reveal(player, "§aRemoved flag " + flag, null);
                                } else {
                                    crop.flags.add(flag);
                                    animator.reveal(player, "§aAdded flag " + flag, null);
                                }
                                crop.dirty = true;
                            } catch (IllegalArgumentException e) {
                                animator.reveal(player, "§cInvalid flag", null);
                            }
                        }
                        case "advance" -> {
                            // Reset timer to allow instant advance
                            crop.stageAdvancedAt = 0;
                            crop.dirty = true;
                            animator.reveal(player, "§aReset advancement timer", null);
                        }
                        default -> animator.reveal(player, "§cUnknown force parameter", null);
                    }
                }
            }
            case "clear" -> {
                if (!player.hasPermission("cultivar.admin")) {
                    animator.reveal(player, "§cNo permission", null);
                    return true;
                }
                int radius = args.length > 1 ? Integer.parseInt(args[1]) : 5;
                List<CropRecord> toRemove = new ArrayList<>();
                for (CropRecord crop : cropManager.getAll()) {
                    if (crop.location.getWorld().equals(player.getWorld()) && crop.location.distance(player.getLocation()) <= radius) {
                        toRemove.add(crop);
                    }
                }
                for (CropRecord crop : toRemove) {
                    try {
                        revertBlock(crop);
                        cropManager.remove(crop.location);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                animator.reveal(player, "§aCleared " + toRemove.size() + " crops in radius " + radius, null);
            }
            default -> animator.reveal(player, "§cUnknown subcommand", null);
        }
        return true;
    }

    private void revertBlock(CropRecord crop) {
        crop.location.getBlock().setType(org.bukkit.Material.AIR);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("cultivar.admin")) return new ArrayList<>();
        if (args.length == 1) {
            return Arrays.asList("inspect", "give", "remove", "reload", "force", "clear");
        }
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "give" -> { return null; } // players
                case "force" -> { return Arrays.asList("stage", "stress", "flag", "advance"); }
                case "clear" -> { return Arrays.asList("5", "10", "20", "50"); }
            }
        }
        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "give" -> { return Arrays.asList("cannabis_seed", "tobacco_seed", "tea_seed", "tea_leaf", "dried_tea_leaf", "compost", "pipe", "teapot"); }
                case "force" -> {
                    if ("flag".equals(args[1].toLowerCase())) {
                        return Arrays.stream(CropFlag.values()).map(Enum::name).collect(Collectors.toList());
                    }
                    if ("stage".equals(args[1].toLowerCase())) {
                        return Arrays.asList("0", "1", "2", "3", "4", "5");
                    }
                }
            }
        }
        return new ArrayList<>();
    }
}