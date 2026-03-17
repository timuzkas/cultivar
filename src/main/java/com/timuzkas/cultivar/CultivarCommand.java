package com.timuzkas.cultivar;

import java.util.*;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CultivarCommand implements CommandExecutor, TabCompleter {

    private final CropManager cropManager;
    private final ActionBarAnimator animator;
    private final org.bukkit.plugin.Plugin plugin;
    private PlayerStrainManager strainManager;
    private SoilManager soilManager;

    public void setStrainManager(PlayerStrainManager strainManager) {
        this.strainManager = strainManager;
    }

    public void setSoilManager(SoilManager soilManager) {
        this.soilManager = soilManager;
    }

    public CultivarCommand(
        CropManager cropManager,
        ActionBarAnimator animator,
        org.bukkit.plugin.Plugin plugin
    ) {
        this.cropManager = cropManager;
        this.animator = animator;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            // List crops
            List<CropRecord> crops = cropManager
                .getAll()
                .stream()
                .filter(c -> c.ownerUuid.equals(player.getUniqueId()))
                .collect(Collectors.toList());
            if (crops.isEmpty()) {
                animator.reveal(player, "§7No active crops", null);
            } else {
                for (CropRecord crop : crops) {
                    String flags = crop.flags.isEmpty()
                        ? ""
                        : " [" +
                          crop.flags
                              .stream()
                              .map(Enum::name)
                              .collect(Collectors.joining(", ")) +
                          "]";
                    animator.reveal(
                        player,
                        "§a" +
                            crop.type.name().toLowerCase() +
                            " at " +
                            crop.location.getBlockX() +
                            "," +
                            crop.location.getBlockZ() +
                            " stage " +
                            crop.stage +
                            flags,
                        null
                    );
                }
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "inspect" -> {
                // Look at crop
                CropRecord crop = cropManager.getByLocation(
                    player.getTargetBlockExact(5).getLocation()
                );
                if (crop == null) {
                    animator.reveal(player, "§cNot looking at a crop", null);
                } else {
                    long now = System.currentTimeMillis();
                    long stageTime = crop.getStageTimeMs(plugin.getConfig(), 0);
                    long timeToAdvance =
                        (crop.stageAdvancedAt + stageTime) - now;

                    long waterExpiry =
                        plugin
                            .getConfig()
                            .getLong(
                                "cultivar." +
                                    crop.type.name().toLowerCase() +
                                    ".water-expiry-minutes",
                                45
                            ) *
                        60000;
                    boolean watered = now - crop.lastWatered <= waterExpiry;
                    org.bukkit.World world = crop.location.getWorld();
                    boolean rained =
                        world.hasStorm() &&
                        world.getHighestBlockYAt(crop.location) <=
                        crop.location.getBlockY();
                    boolean isWatered = watered || rained;
                    int light = crop.location.getBlock().getLightLevel();

                    int minLight = switch (crop.type) {
                        case CANNABIS -> 10;
                        case TOBACCO -> crop.flags.contains(CropFlag.LOW_LIGHT)
                            ? 0
                            : 12; // Allow low if flagged
                        case TEA -> 7;
                        case MUSHROOM -> 0;
                    };
                    int maxLight = switch (crop.type) {
                        case TEA -> 13;
                        default -> 15;
                    };
                    boolean lightOk = light >= minLight && light <= maxLight;

                    String wateredStr = isWatered ? "§bWatered" : "§cDry";
                    String lightStr = "§eLight: " + light;
                    String readyStr = (timeToAdvance <= 0) ? " §a[Ready]" : "";

                    String status =
                        "§a" +
                        crop.type.name().toLowerCase() +
                        " stage " +
                        crop.stage +
                        " stress " +
                        crop.stress +
                        " [" +
                        wateredStr +
                        "§a] " +
                        lightStr +
                        readyStr;

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
                        if (
                            crop.type == CropType.CANNABIS &&
                            (crop.stage == 3 || crop.stage == 4) &&
                            crop.flags.contains(CropFlag.NEEDS_PRUNING)
                        ) {
                            status += " §cPaused: Pruning Needed";
                        }
                        if (
                            crop.type == CropType.TOBACCO &&
                            (crop.stage == 2 ||
                                crop.stage == 4 ||
                                crop.stage == 5) &&
                            crop.flags.contains(CropFlag.NEEDS_STRIPPING)
                        ) {
                            status += " §cPaused: Stripping Needed";
                        }
                        if (
                            crop.type == CropType.TEA &&
                            crop.flags.contains(CropFlag.NEEDS_MISTING)
                        ) {
                            status += " §cPaused: Misting Needed";
                        }
                    }

                    if (!crop.flags.isEmpty()) {
                        status +=
                            " flags: " +
                            crop.flags
                                .stream()
                                .map(Enum::name)
                                .collect(Collectors.joining(", "));
                    }
                    if (timeToAdvance > 0) {
                        status +=
                            " next advance in " + (timeToAdvance / 1000) + "s";
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
                    animator.reveal(
                        player,
                        "§cUsage: /cv give <player> <item> [amount]",
                        null
                    );
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    animator.reveal(player, "§cPlayer not found", null);
                    return true;
                }
                int amount =
                    args.length >= 4
                        ? Math.max(1, Integer.parseInt(args[3]))
                        : 1;
                String itemArg = args[2].toLowerCase();
                ItemStack item = switch (itemArg) {
                    case "cannabis_seed" -> ItemFactory.createCannabisSeed();
                    case "tobacco_seed" -> ItemFactory.createTobaccoSeed();
                    case "tea_seed" -> ItemFactory.createTeaSeed();
                    case "tea_leaf" -> ItemFactory.createFreshTeaLeaf();
                    case "dried_tea_leaf" -> ItemFactory.createDriedTeaLeaf();
                    case "compost" -> ItemFactory.createCompost();
                    case "pipe", "wood_pipe" -> ItemFactory.createBlankPipe(
                        PipeTier.WOOD
                    );
                    case "clay_pipe" -> ItemFactory.createClayPipe();
                    case "meerschaum_pipe" -> ItemFactory.createMeerschaumPipe();
                    case "teapot" -> ItemFactory.createEmptyTeapot();
                    case "spore" -> ItemFactory.createSporeItem();
                    case "mushroom_seed" -> ItemFactory.createMushroomSeed();
                    case "dried_mushroom" -> ItemFactory.createDriedMushroom();
                    case
                        "basket",
                        "harvest_basket" -> ItemFactory.createHarvestBasket();
                    default -> {
                        if (itemArg.startsWith("cannabis_seed:")) {
                            String strainId = itemArg.substring(14);
                            yield ItemFactory.createCannabisSeed(
                                strainId,
                                StrainProfile.generate(strainId).name
                            );
                        }
                        if (itemArg.startsWith("light_cured_leaf")) {
                            yield createCuredLeaf("light");
                        }
                        if (itemArg.startsWith("dark_cured_leaf")) {
                            yield createCuredLeaf("dark");
                        }
                        if (itemArg.startsWith("fire_cured_leaf")) {
                            yield createCuredLeaf("fire");
                        }
                        yield null;
                    }
                };
                if (item == null) {
                    animator.reveal(player, "§cInvalid item", null);
                } else {
                    item.setAmount(amount);
                    target.getInventory().addItem(item);
                    animator.reveal(
                        player,
                        "§aGiven " +
                            amount +
                            " " +
                            args[2] +
                            " to " +
                            target.getName(),
                        null
                    );
                }
            }
            case "remove" -> {
                if (!player.hasPermission("cultivar.admin")) {
                    animator.reveal(player, "§cNo permission", null);
                    return true;
                }
                CropRecord crop = cropManager.getByLocation(
                    player.getTargetBlockExact(5).getLocation()
                );
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
                    animator.reveal(
                        player,
                        "§cUsage: /cv force <stage|stress|flag|advance|strain|cure|steep|mushroom> <value>",
                        null
                    );
                    return true;
                }
                org.bukkit.block.Block targetBlock = player.getTargetBlockExact(
                    5
                );
                String sub = args[1].toLowerCase();

                switch (sub) {
                    case "strain" -> {
                        if (targetBlock == null) {
                            animator.reveal(
                                player,
                                "§cNot looking at a block",
                                null
                            );
                            return true;
                        }
                        CropRecord crop = cropManager.getByLocation(
                            targetBlock.getLocation()
                        );
                        if (crop == null) {
                            animator.reveal(
                                player,
                                "§cNot looking at a crop",
                                null
                            );
                            return true;
                        }
                        if (crop.type != CropType.CANNABIS) {
                            animator.reveal(
                                player,
                                "§cOnly cannabis has strains",
                                null
                            );
                            return true;
                        }
                        crop.strainId = args[2];
                        crop.strainName = StrainProfile.generate(
                            args[2] + "_force"
                        ).name;
                        crop.dirty = true;
                        animator.reveal(
                            player,
                            "§aSet strain to " +
                                crop.strainName +
                                " (§7" +
                                crop.strainId +
                                "§a)",
                            null
                        );
                    }
                    case "cure" -> {
                        ItemStack hand = player
                            .getInventory()
                            .getItemInMainHand();
                        if (!ItemFactory.isDryTobaccoLeaf(hand)) {
                            animator.reveal(
                                player,
                                "§cNot holding dry tobacco leaf",
                                null
                            );
                            return true;
                        }
                        String cureType = args[2].toLowerCase();
                        if (!cureType.matches("light|dark|fire|generic")) {
                            animator.reveal(
                                player,
                                "§cUsage: /cv force cure <light|dark|fire|generic>",
                                null
                            );
                            return true;
                        }
                        ItemMeta meta = hand.getItemMeta();
                        if (meta != null) {
                            PersistentDataContainer pdc =
                                meta.getPersistentDataContainer();
                            pdc.set(
                                new NamespacedKey("cultivar", "cure_type"),
                                PersistentDataType.STRING,
                                cureType
                            );
                            String displayName = switch (cureType) {
                                case "light" -> "§7Light-cured Leaf";
                                case "dark" -> "§6Dark-cured Leaf";
                                case "fire" -> "§cFire-cured Leaf";
                                default -> "§7Dry Tobacco Leaf";
                            };
                            meta.setDisplayName(displayName);
                            hand.setItemMeta(meta);
                        }
                        animator.reveal(
                            player,
                            "§aSet cure type to " + cureType,
                            null
                        );
                    }
                    case "steep" -> {
                        if (args.length < 3) {
                            animator.reveal(
                                player,
                                "§cUsage: /cv force steep <weak|perfect|bitter>",
                                null
                            );
                            return true;
                        }
                        if (
                            targetBlock == null ||
                            (targetBlock.getType() != Material.CAULDRON &&
                                targetBlock.getType() !=
                                Material.WATER_CAULDRON)
                        ) {
                            animator.reveal(
                                player,
                                "§cNot looking at a cauldron",
                                null
                            );
                            return true;
                        }
                        animator.reveal(
                            player,
                            "§eNote: Use /cv give to create a brewed teapot, then the quality will be applied",
                            null
                        );
                    }
                    case "mushroom" -> {
                        if (targetBlock == null) {
                            animator.reveal(
                                player,
                                "§cNot looking at a block",
                                null
                            );
                            return true;
                        }
                        CropRecord crop = cropManager.getByLocation(
                            targetBlock.getLocation()
                        );
                        if (crop == null) {
                            animator.reveal(
                                player,
                                "§cNot looking at a crop",
                                null
                            );
                            return true;
                        }
                        if (crop.type != CropType.MUSHROOM) {
                            animator.reveal(
                                player,
                                "§cOnly mushroom crops have click counters",
                                null
                            );
                            return true;
                        }
                        int clickCount = Integer.parseInt(args[2]);
                        if (clickCount > 3) {
                            crop.stress += 1;
                            crop.flags.add(CropFlag.STUNTED);
                            animator.reveal(
                                player,
                                "§cMushroom stressed! (>3 clicks)",
                                null
                            );
                        }
                        crop.dirty = true;
                        animator.reveal(
                            player,
                            "§aSet mushroom click count to " + clickCount,
                            null
                        );
                    }
                    default -> {
                        if (targetBlock == null) {
                            animator.reveal(
                                player,
                                "§cNot looking at a block",
                                null
                            );
                            return true;
                        }
                        CropRecord crop = cropManager.getByLocation(
                            targetBlock.getLocation()
                        );
                        if (crop == null) {
                            animator.reveal(
                                player,
                                "§cNot looking at a crop",
                                null
                            );
                            return true;
                        }
                        String val = args[2];
                        switch (sub) {
                            case "stage" -> {
                                int stage = Integer.parseInt(val);
                                crop.stage = stage;
                                crop.stageAdvancedAt =
                                    System.currentTimeMillis();
                                crop.dirty = true;
                                crop.location
                                    .getBlock()
                                    .setType(crop.type.getVisualBlock(stage));
                                animator.reveal(
                                    player,
                                    "§aForced stage to " + stage,
                                    null
                                );
                            }
                            case "stress" -> {
                                int stress = Integer.parseInt(val);
                                crop.stress = stress;
                                crop.dirty = true;
                                animator.reveal(
                                    player,
                                    "§aForced stress to " + stress,
                                    null
                                );
                            }
                            case "flag" -> {
                                try {
                                    CropFlag flag = CropFlag.valueOf(
                                        val.toUpperCase()
                                    );
                                    if (crop.flags.contains(flag)) {
                                        crop.flags.remove(flag);
                                        animator.reveal(
                                            player,
                                            "§aRemoved flag " + flag,
                                            null
                                        );
                                    } else {
                                        crop.flags.add(flag);
                                        animator.reveal(
                                            player,
                                            "§aAdded flag " + flag,
                                            null
                                        );
                                    }
                                    crop.dirty = true;
                                } catch (IllegalArgumentException e) {
                                    animator.reveal(
                                        player,
                                        "§cInvalid flag",
                                        null
                                    );
                                }
                            }
                            case "advance" -> {
                                crop.stageAdvancedAt = 0;
                                crop.dirty = true;
                                animator.reveal(
                                    player,
                                    "§aReset advancement timer",
                                    null
                                );
                            }
                            default -> animator.reveal(
                                player,
                                "§cUnknown force parameter",
                                null
                            );
                        }
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
                    if (
                        crop.location.getWorld().equals(player.getWorld()) &&
                        crop.location.distance(player.getLocation()) <= radius
                    ) {
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
                animator.reveal(
                    player,
                    "§aCleared " +
                        toRemove.size() +
                        " crops in radius " +
                        radius,
                    null
                );
            }
            case "strains" -> {
                if (args.length > 1 && args[1].equalsIgnoreCase("list")) {
                    if (!player.hasPermission("cultivar.admin")) {
                        animator.reveal(player, "§cNo permission", null);
                        return true;
                    }
                    Set<String> knownStrains =
                        strainManager.getAllKnownStrains();
                    if (knownStrains.isEmpty()) {
                        animator.reveal(
                            player,
                            "§7No strains discovered yet",
                            null
                        );
                    } else {
                        for (String strainId : knownStrains) {
                            StrainProfile strain = StrainProfile.generate(
                                strainId + "_known"
                            );
                            animator.reveal(
                                player,
                                "§6" +
                                    strain.name +
                                    " §7(ID: " +
                                    strainId +
                                    ") - yield:" +
                                    String.format(
                                        "%.1f",
                                        strain.yieldBonus * 100
                                    ) +
                                    "% speed:" +
                                    String.format(
                                        "%.0f",
                                        strain.speedMultiplier * 100
                                    ) +
                                    "%",
                                null
                            );
                        }
                    }
                    return true;
                }
                if (args.length > 1 && args[1].equalsIgnoreCase("reset")) {
                    if (!player.hasPermission("cultivar.admin")) {
                        animator.reveal(player, "§cNo permission", null);
                        return true;
                    }
                    if (args.length < 3) {
                        animator.reveal(
                            player,
                            "§cUsage: /cv strains reset <player>",
                            null
                        );
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        animator.reveal(player, "§cPlayer not found", null);
                        return true;
                    }
                    strainManager.clearPlayerStrains(target.getUniqueId());
                    animator.reveal(
                        player,
                        "§aCleared strains for " + target.getName(),
                        null
                    );
                    return true;
                }
                Set<String> discovered = strainManager.getDiscoveredStrains(
                    player.getUniqueId()
                );
                if (discovered.isEmpty()) {
                    animator.reveal(
                        player,
                        "§7You haven't discovered any strains yet",
                        null
                    );
                } else {
                    animator.reveal(
                        player,
                        "§6=== Your Discovered Strains ===",
                        null
                    );
                    for (String strainId : discovered) {
                        StrainProfile strain = StrainProfile.generate(
                            strainId + "_display"
                        );
                        String bonuses = "§7";
                        if (strain.yieldBonus > 0.2) bonuses += "High Yield ";
                        if (strain.speedMultiplier < 0.9) bonuses += "Fast ";
                        if (strain.stressResistance > 0) bonuses +=
                            "Resilient ";
                        if (bonuses.equals("§7")) bonuses = "§7Balanced";
                        animator.reveal(
                            player,
                            "§a" + strain.name + " - " + bonuses,
                            null
                        );
                    }
                }
            }
            case "journal" -> {
                CropRecord crop = cropManager.getByLocation(
                    player.getTargetBlockExact(5).getLocation()
                );
                if (crop == null) {
                    animator.reveal(player, "§cNot looking at a crop", null);
                    return true;
                }
                String info =
                    "§6=== Crop Info ===\n" +
                    "§aType: " +
                    crop.type.name() +
                    "\n" +
                    "§aStage: " +
                    crop.stage +
                    "/" +
                    crop.type.getMaxStage() +
                    "\n" +
                    "§aStress: " +
                    crop.stress;
                if (crop.strainName != null) {
                    info += "\n§aStrain: " + crop.strainName;
                    strainManager.addDiscoveredStrain(
                        player.getUniqueId(),
                        crop.strainId
                    );
                }
                if (!crop.flags.isEmpty()) {
                    info +=
                        "\n§cFlags: " +
                        crop.flags
                            .stream()
                            .map(Enum::name)
                            .collect(Collectors.joining(", "));
                }
                for (String line : info.split("\n")) {
                    animator.reveal(player, line, null);
                }
            }
            case "soil" -> {
                if (soilManager == null) {
                    animator.reveal(
                        player,
                        "§cSoil manager not available",
                        null
                    );
                    return true;
                }
                if (args.length < 2) {
                    animator.reveal(
                        player,
                        "§cUsage: /cv soil <get|set|clear>",
                        null
                    );
                    return true;
                }
                org.bukkit.block.Block targetBlock = player.getTargetBlockExact(
                    5
                );
                if (
                    targetBlock == null ||
                    targetBlock.getType() != Material.FARMLAND
                ) {
                    animator.reveal(player, "§cNot looking at farmland", null);
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "get" -> {
                        int level = soilManager.getEnrichment(
                            targetBlock.getLocation()
                        );
                        String stars =
                            "§a" +
                            "✦".repeat(level) +
                            "§7" +
                            "✧".repeat(3 - level);
                        animator.reveal(
                            player,
                            "§eEnrichment Level: " + level + " " + stars,
                            null
                        );
                    }
                    case "set" -> {
                        if (args.length < 3) {
                            animator.reveal(
                                player,
                                "§cUsage: /cv soil set <0-3>",
                                null
                            );
                            return true;
                        }
                        int level = Integer.parseInt(args[2]);
                        level = Math.max(0, Math.min(3, level));
                        soilManager.setEnrichment(
                            targetBlock.getLocation(),
                            level
                        );
                        animator.reveal(
                            player,
                            "§aSet enrichment to " + level,
                            null
                        );
                    }
                    case "clear" -> {
                        soilManager.setEnrichment(targetBlock.getLocation(), 0);
                        animator.reveal(player, "§aCleared enrichment", null);
                    }
                    default -> animator.reveal(
                        player,
                        "§cUsage: /cv soil <get|set|clear>",
                        null
                    );
                }
            }
            default -> animator.reveal(player, "§cUnknown subcommand", null);
        }
        return true;
    }

    private void revertBlock(CropRecord crop) {
        crop.location.getBlock().setType(org.bukkit.Material.AIR);
    }

    private ItemStack createCuredLeaf(String cureType) {
        ItemStack item = ItemFactory.createDryTobaccoLeaf();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(
                new NamespacedKey("cultivar", "cure_type"),
                PersistentDataType.STRING,
                cureType
            );
            String displayName = switch (cureType) {
                case "light" -> "§7Light-cured Leaf";
                case "dark" -> "§6Dark-cured Leaf";
                case "fire" -> "§cFire-cured Leaf";
                default -> "§7Dry Tobacco Leaf";
            };
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public List<String> onTabComplete(
        CommandSender sender,
        Command command,
        String alias,
        String[] args
    ) {
        List<String> empty = new ArrayList<>();
        if (args.length == 1) {
            List<String> cmds = new ArrayList<>(
                Arrays.asList(
                    "inspect",
                    "give",
                    "remove",
                    "reload",
                    "force",
                    "clear",
                    "strains",
                    "journal",
                    "soil"
                )
            );
            if (sender.hasPermission("cultivar.admin")) {
                return cmds;
            }
            return cmds
                .stream()
                .filter(c -> !c.equals("give"))
                .collect(Collectors.toList());
        }
        if (args.length == 2) {
            if ("strains".equals(args[0].toLowerCase())) {
                if (sender.hasPermission("cultivar.admin")) {
                    return Arrays.asList("list", "reset");
                }
                return empty;
            }
            if ("soil".equals(args[0].toLowerCase())) {
                return Arrays.asList("get", "set", "clear");
            }
            if (!sender.hasPermission("cultivar.admin")) return empty;
            switch (args[0].toLowerCase()) {
                case "give" -> {
                    return null;
                }
                case "force" -> {
                    return Arrays.asList(
                        "stage",
                        "stress",
                        "flag",
                        "advance",
                        "strain",
                        "cure",
                        "steep",
                        "mushroom"
                    );
                }
                case "clear" -> {
                    return Arrays.asList("5", "10", "20", "50");
                }
            }
        }
        if (args.length == 3) {
            if (!sender.hasPermission("cultivar.admin")) return empty;
            switch (args[0].toLowerCase()) {
                case "give" -> {
                    return Arrays.asList(
                        "cannabis_seed",
                        "tobacco_seed",
                        "tea_seed",
                        "tea_leaf",
                        "dried_tea_leaf",
                        "compost",
                        "pipe",
                        "clay_pipe",
                        "meerschaum_pipe",
                        "teapot",
                        "spore",
                        "mushroom_seed",
                        "dried_mushroom",
                        "basket",
                        "cannabis_seed:strain123",
                        "light_cured_leaf",
                        "dark_cured_leaf",
                        "fire_cured_leaf"
                    );
                }
                case "force" -> {
                    if ("cure".equals(args[1].toLowerCase())) {
                        return Arrays.asList(
                            "light",
                            "dark",
                            "fire",
                            "generic"
                        );
                    }
                    if ("steep".equals(args[1].toLowerCase())) {
                        return Arrays.asList("weak", "perfect", "bitter");
                    }
                    if ("flag".equals(args[1].toLowerCase())) {
                        return Arrays.stream(CropFlag.values())
                            .map(Enum::name)
                            .collect(Collectors.toList());
                    }
                    if ("stage".equals(args[1].toLowerCase())) {
                        return Arrays.asList("0", "1", "2", "3", "4", "5");
                    }
                    if ("soil".equals(args[1].toLowerCase())) {
                        return Arrays.asList("0", "1", "2", "3");
                    }
                }
            }
        }
        if (
            args.length == 4 &&
            args[0].equalsIgnoreCase("force") &&
            args[1].equalsIgnoreCase("mushroom")
        ) {
            return Arrays.asList("0", "1", "2", "3", "4", "5");
        }
        return empty;
    }
}
