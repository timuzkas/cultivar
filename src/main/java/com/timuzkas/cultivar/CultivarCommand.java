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
    private GrowerManager growerManager;

    public void setStrainManager(PlayerStrainManager strainManager) {
        this.strainManager = strainManager;
    }

    public void setSoilManager(SoilManager soilManager) {
        this.soilManager = soilManager;
    }

    public void setGrowerManager(GrowerManager growerManager) {
        this.growerManager = growerManager;
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
            if (growerManager != null) {
                int score = growerManager.getScore(player.getUniqueId());
                String title = growerManager.getTitle(player.getUniqueId());
                player.sendMessage("§2§lCultivar Profile");
                player.sendMessage(
                    " §8» §7Rank: §6" +
                        title +
                        " §8(§7Score: §f" +
                        score +
                        "§8)"
                );
                player.sendMessage("");
            }

            List<CropRecord> crops = cropManager
                .getAll()
                .stream()
                .filter(c -> c.ownerUuid.equals(player.getUniqueId()))
                .collect(Collectors.toList());
            if (crops.isEmpty()) {
                player.sendMessage(" §8» §7No active crops discovered.");
            } else {
                player.sendMessage("§2§lYour Crops");
                for (CropRecord crop : crops) {
                    String flags = crop.flags.isEmpty()
                        ? ""
                        : " §8[§7" +
                          crop.flags
                              .stream()
                              .map(Enum::name)
                              .collect(Collectors.joining(", ")) +
                          "§8]";
                    player.sendMessage(
                        " §8• §a" +
                            crop.type.name().toLowerCase() +
                            " §8at §7" +
                            crop.location.getBlockX() +
                            "§8,§7" +
                            crop.location.getBlockZ() +
                            " §8stage §f" +
                            crop.stage +
                            flags
                    );
                }
            }
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "inspect" -> {
                CropRecord crop = cropManager.getByLocation(
                    player.getTargetBlockExact(5).getLocation()
                );
                if (crop == null) {
                    player.sendMessage("§8» §cNot looking at a crop.");
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
                            : 12;
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

                    player.sendMessage(
                        "§2§l" +
                            crop.type.name() +
                            " §8(Stage " +
                            crop.stage +
                            ")"
                    );
                    player.sendMessage(
                        " §8• §7Status: " +
                            wateredStr +
                            " §8| " +
                            lightStr +
                            readyStr
                    );
                    player.sendMessage(" §8• §7Stress: §f" + crop.stress);

                    if (timeToAdvance <= 0) {
                        if (!isWatered) player.sendMessage(
                            " §8» §eNeeds Water"
                        );
                        if (!lightOk) player.sendMessage(
                            " §8» §eLight Level Incorrect"
                        );
                        if (
                            crop.flags.contains(CropFlag.DYING)
                        ) player.sendMessage(" §8» §cDying");
                        if (
                            crop.type == CropType.CANNABIS &&
                            (crop.stage == 3 || crop.stage == 4) &&
                            crop.flags.contains(CropFlag.NEEDS_PRUNING)
                        ) {
                            player.sendMessage(" §8» §eNeeds Pruning");
                        }
                    }

                    if (timeToAdvance > 0) {
                        player.sendMessage(
                            " §8• §7Advance in: §f" +
                                (timeToAdvance / 1000) +
                                "s"
                        );
                    }
                }
            }
            case "give" -> {
                if (!player.hasPermission("cultivar.admin")) {
                    player.sendMessage("§8» §cInsufficient permission.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(
                        "§8» §cUsage: /cv give <player> <item> [amount]"
                    );
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§8» §cPlayer not found.");
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
                            StrainProfile strain = StrainProfile.generate(strainId, CropType.CANNABIS);
                            yield ItemFactory.createCannabisSeed(
                                strainId,
                                strain.name
                            );
                        }
                        if (itemArg.startsWith("tobacco_seed:")) {
                            String strainId = itemArg.substring(12);
                            StrainProfile strain = StrainProfile.generate(strainId, CropType.TOBACCO);
                            yield ItemFactory.createTobaccoSeed(
                                strainId,
                                strain.name
                            );
                        }
                        if (itemArg.startsWith("tea_seed:")) {
                            String strainId = itemArg.substring(8);
                            StrainProfile strain = StrainProfile.generate(strainId, CropType.TEA);
                            yield ItemFactory.createTeaSeed(
                                strainId,
                                strain.name
                            );
                        }
                        if (itemArg.startsWith("mushroom_seed:")) {
                            String strainId = itemArg.substring(14);
                            StrainProfile strain = StrainProfile.generate(strainId, CropType.MUSHROOM);
                            yield ItemFactory.createMushroomSeed(
                                strainId,
                                strain.name
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
                        if (itemArg.startsWith("tea_bottle:")) {
                            String variant = itemArg.substring(11);
                            yield ItemFactory.createCupOfTea(variant);
                        }
                        yield null;
                    }
                };
                if (item == null) {
                    player.sendMessage("§8» §cInvalid item specified.");
                } else {
                    item.setAmount(amount);
                    target.getInventory().addItem(item);
                    player.sendMessage(
                        "§8» §7Gave §a" +
                            amount +
                            "x " +
                            args[2] +
                            " §7to §f" +
                            target.getName()
                    );
                }
            }
            case "remove" -> {
                if (!player.hasPermission("cultivar.admin")) {
                    player.sendMessage("§8» §cInsufficient permission.");
                    return true;
                }
                CropRecord crop = cropManager.getByLocation(
                    player.getTargetBlockExact(5).getLocation()
                );
                if (crop == null) {
                    player.sendMessage("§8» §cNot looking at a crop.");
                } else {
                    try {
                        revertBlock(crop);
                        cropManager.remove(crop.location);
                        player.sendMessage("§8» §aCrop removed successfully.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage("§8» §cError while removing crop.");
                    }
                }
            }
            case "reload" -> {
                if (!player.hasPermission("cultivar.admin")) {
                    player.sendMessage("§8» §cInsufficient permission.");
                    return true;
                }
                plugin.reloadConfig();
                player.sendMessage("§8» §aConfiguration reloaded.");
            }
            case "force" -> {
                if (!player.hasPermission("cultivar.admin")) {
                    player.sendMessage("§8» §cInsufficient permission.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(
                        "§8» §cUsage: /cv force <stage|stress|flag|advance|strain|cure|steep|mushroom> <value>"
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
                            player.sendMessage("§8» §cTarget block null.");
                            return true;
                        }
                        CropRecord crop = cropManager.getByLocation(
                            targetBlock.getLocation()
                        );
                        if (crop == null) {
                            player.sendMessage("§8» §cNot looking at a crop.");
                            return true;
                        }
                        if (crop.type != CropType.CANNABIS) {
                            player.sendMessage(
                                "§8» §cOnly Cannabis supports strains."
                            );
                            return true;
                        }
                        crop.strainId = args[2];
                        crop.strainName = StrainProfile.generate(
                            args[2] + "_force"
                        ).name;
                        crop.dirty = true;
                        player.sendMessage(
                            "§8» §7Strain updated: §a" +
                                crop.strainName +
                                " §8(§7" +
                                crop.strainId +
                                "§8)"
                        );
                    }
                    case "cure" -> {
                        ItemStack hand = player
                            .getInventory()
                            .getItemInMainHand();
                        if (!ItemFactory.isDryTobaccoLeaf(hand)) {
                            player.sendMessage(
                                "§8» §cNot holding Dry Tobacco Leaf."
                            );
                            return true;
                        }
                        String cureType = args[2].toLowerCase();
                        if (!cureType.matches("light|dark|fire|generic")) {
                            player.sendMessage(
                                "§8» §cUse: light, dark, fire, generic"
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
                        player.sendMessage(
                            "§8» §7Cure type forced: §a" + cureType
                        );
                    }
                    case "steep" -> {
                        if (args.length < 3) {
                            player.sendMessage(
                                "§8» §cUsage: /cv force steep <weak|perfect|bitter>"
                            );
                            return true;
                        }
                        if (
                            targetBlock == null ||
                            (targetBlock.getType() != Material.CAULDRON &&
                                targetBlock.getType() !=
                                Material.WATER_CAULDRON)
                        ) {
                            player.sendMessage(
                                "§8» §cNot looking at a cauldron."
                            );
                            return true;
                        }
                        player.sendMessage(
                            "§8» §eQuality applies when bottling from the teapot."
                        );
                    }
                    case "mushroom" -> {
                        if (targetBlock == null) {
                            player.sendMessage("§8» §cTarget block null.");
                            return true;
                        }
                        CropRecord crop = cropManager.getByLocation(
                            targetBlock.getLocation()
                        );
                        if (crop == null) {
                            player.sendMessage("§8» §cNot looking at a crop.");
                            return true;
                        }
                        if (crop.type != CropType.MUSHROOM) {
                            player.sendMessage(
                                "§8» §cOnly Mushrooms track click count."
                            );
                            return true;
                        }
                        int clickCount = Integer.parseInt(args[2]);
                        if (clickCount > 3) {
                            crop.stress += 1;
                            crop.flags.add(CropFlag.STUNTED);
                            player.sendMessage(
                                "§8» §cMushroom stressed by over-interaction."
                            );
                        }
                        crop.dirty = true;
                        player.sendMessage(
                            "§8» §7Click counter set to: §f" + clickCount
                        );
                    }
                    default -> {
                        if (targetBlock == null) {
                            player.sendMessage("§8» §cTarget block null.");
                            return true;
                        }
                        CropRecord crop = cropManager.getByLocation(
                            targetBlock.getLocation()
                        );
                        if (crop == null) {
                            player.sendMessage("§8» §cNot looking at a crop.");
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
                                player.sendMessage(
                                    "§8» §7Stage forced: §a" + stage
                                );
                            }
                            case "stress" -> {
                                int stress = Integer.parseInt(val);
                                crop.stress = stress;
                                crop.dirty = true;
                                player.sendMessage(
                                    "§8» §7Stress forced: §a" + stress
                                );
                            }
                            case "flag" -> {
                                try {
                                    CropFlag flag = CropFlag.valueOf(
                                        val.toUpperCase()
                                    );
                                    if (crop.flags.contains(flag)) {
                                        crop.flags.remove(flag);
                                        player.sendMessage(
                                            "§8» §7Flag removed: §f" + flag
                                        );
                                    } else {
                                        crop.flags.add(flag);
                                        player.sendMessage(
                                            "§8» §7Flag added: §a" + flag
                                        );
                                    }
                                    crop.dirty = true;
                                } catch (IllegalArgumentException e) {
                                    player.sendMessage("§8» §cUnknown flag.");
                                }
                            }
                            case "advance" -> {
                                crop.stageAdvancedAt = 0;
                                crop.dirty = true;
                                player.sendMessage(
                                    "§8» §7Advancement timer reset."
                                );
                            }
                            default -> player.sendMessage(
                                "§8» §cUnknown parameter."
                            );
                        }
                    }
                }
            }
            case "clear" -> {
                if (!player.hasPermission("cultivar.admin")) {
                    player.sendMessage("§8» §cInsufficient permission.");
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
                player.sendMessage(
                    "§8» §7Cleared §a" +
                        toRemove.size() +
                        " §7crops within radius §f" +
                        radius +
                        "§7."
                );
            }
            case "strains" -> {
                if (args.length > 1 && args[1].equalsIgnoreCase("list")) {
                    if (!player.hasPermission("cultivar.admin")) {
                        player.sendMessage("§8» §cInsufficient permission.");
                        return true;
                    }
                    
                    UUID targetUuid = player.getUniqueId();
                    String targetName = player.getName();
                    
                    if (args.length > 2) {
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target == null) {
                            player.sendMessage("§8» §cPlayer not found.");
                            return true;
                        }
                        targetUuid = target.getUniqueId();
                        targetName = target.getName();
                    }
                    
                    strainManager.loadPlayerStrains(targetUuid);
                    Set<String> knownStrains = strainManager.getDiscoveredStrains(targetUuid);
                    
                    if (knownStrains.isEmpty()) {
                        player.sendMessage("§8» §7No strains in §f" + targetName + "§7's journal.");
                    } else {
                        player.sendMessage("§6§lStrains: §f" + targetName + " §8(" + knownStrains.size() + ")");
                        
                        for (String strainId : knownStrains) {
                            CropType cropType = strainManager.getStrainCropType(targetUuid, strainId);
                            StrainProfile strain = StrainProfile.generate(strainId, cropType);
                            
                            net.md_5.bungee.api.chat.TextComponent line = new net.md_5.bungee.api.chat.TextComponent(
                                " §8• §a" + strain.name + " §8[§7" + cropType.name() + "§8] §n§9" + strainId + "§r"
                            );
                            line.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                                net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                                new net.md_5.bungee.api.chat.ComponentBuilder("§7Click to view: §f/cv strains info " + strainId).create()
                            ));
                            line.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                                net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND,
                                "/cv strains info " + strainId
                            ));
                            
                            player.spigot().sendMessage(line);
                        }
                    }
                    return true;
                }
                if (args.length > 1 && args[1].equalsIgnoreCase("info")) {
                    if (!player.hasPermission("cultivar.admin")) {
                        player.sendMessage("§8» §cInsufficient permission.");
                        return true;
                    }
                    if (args.length < 3) {
                        player.sendMessage("§8» §cUsage: /cv strains info <strainId> [cropType]");
                        return true;
                    }
                    String strainId = args[2];
                    
                    CropType cropType = CropType.CANNABIS;
                    
                    if (args.length > 3) {
                        try {
                            cropType = CropType.valueOf(args[3].toUpperCase());
                        } catch (Exception e) {
                            cropType = null;
                        }
                    }
                    
                    if (cropType == null) {
                        cropType = strainManager.getStrainCropType(player.getUniqueId(), strainId);
                    }
                    
                    StrainProfile strain = StrainProfile.generate(strainId, cropType);
                    if (strain == null) {
                        player.sendMessage("§8» §cStrain not found.");
                        return true;
                    }
                    
                    player.sendMessage("§6§lStrain Info: " + strain.name + " §8(§7" + strainId + "§8)");
                    player.sendMessage(" §8• §7Crop: §f" + cropType.name());
                    
                    switch (strain.cropType) {
                        case CANNABIS -> {
                            player.sendMessage(" §8• §7Yield: §f+" + String.format("%.0f%%", strain.yieldBonus * 100));
                            player.sendMessage(" §8• §7Speed: §f" + String.format("%.0f%%", strain.speedMultiplier * 100) + "x");
                            player.sendMessage(" §8• §7Stress Res: §f+" + strain.stressResistance);
                            player.sendMessage(" §8• §7Potency: §f" + strain.potency);
                        }
                        case TOBACCO -> {
                            player.sendMessage(" §8• §7Curability: §f+" + String.format("%.0f%%", strain.curabilityBonus * 100));
                            player.sendMessage(" §8• §7Leaf Yield: §f+" + strain.leafYieldBonus);
                            player.sendMessage(" §8• §7Aroma: §f" + strain.aromaProfile);
                        }
                        case TEA -> {
                            player.sendMessage(" §8• §7Brew Strength: §f+" + String.format("%.0f%%", strain.brewStrength * 100));
                            player.sendMessage(" §8• §7Rarity: §f" + strain.rarityTag);
                        }
                        case MUSHROOM -> {
                            player.sendMessage(" §8• §7Potency: §f+" + strain.potencyLevel);
                            player.sendMessage(" §8• §7Light Tolerance: §f+" + strain.lightTolerance);
                            player.sendMessage(" §8• §7Spore Density: §f+" + strain.sporeDensity);
                        }
                        default -> {}
                    }
                    return true;
                }
                if (args.length > 1 && args[1].equalsIgnoreCase("give")) {
                    if (!player.hasPermission("cultivar.admin")) {
                        player.sendMessage("§8» §cInsufficient permission.");
                        return true;
                    }
                    if (args.length < 4) {
                        player.sendMessage("§8» §cUsage: /cv strains give <player> <strainId> [cropType]");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        player.sendMessage("§8» §cPlayer not found.");
                        return true;
                    }
                    String strainId = args[3];
                    CropType cropType = CropType.CANNABIS;
                    
                    if (args.length > 4) {
                        try {
                            cropType = CropType.valueOf(args[4].toUpperCase());
                        } catch (Exception e) {
                            cropType = strainManager.getStrainCropType(player.getUniqueId(), strainId);
                        }
                    } else {
                        cropType = strainManager.getStrainCropType(player.getUniqueId(), strainId);
                    }
                    
                    StrainProfile strain = StrainProfile.generate(strainId, cropType);
                    
                    ItemStack seed = switch (cropType) {
                        case CANNABIS -> ItemFactory.createCannabisSeed(strainId, strain.name);
                        case TOBACCO -> ItemFactory.createTobaccoSeed(strainId, strain.name);
                        case TEA -> ItemFactory.createTeaSeed(strainId, strain.name);
                        case MUSHROOM -> ItemFactory.createMushroomSeed(strainId, strain.name);
                        default -> ItemFactory.createCannabisSeed(strainId, strain.name);
                    };
                    target.getInventory().addItem(seed);
                    player.sendMessage("§8» §7Gave strain §a" + strain.name + " §7to §f" + target.getName());
                    return true;
                }
                if (args.length > 1 && args[1].equalsIgnoreCase("remove")) {
                    if (!player.hasPermission("cultivar.admin")) {
                        player.sendMessage("§8» §cInsufficient permission.");
                        return true;
                    }
                    if (args.length < 3) {
                        player.sendMessage("§8» §cUsage: /cv strains remove <strainId>");
                        return true;
                    }
                    String strainId = args[2];
                    strainManager.loadPlayerStrains(player.getUniqueId());
                    Set<String> knownStrains = strainManager.getDiscoveredStrains(player.getUniqueId());
                    if (knownStrains.contains(strainId)) {
                        knownStrains.remove(strainId);
                        player.sendMessage("§8» §7Removed strain §a" + strainId + " §7from your journal.");
                    } else {
                        player.sendMessage("§8» §cStrain not found in your journal.");
                    }
                    return true;
                }
                if (args.length > 1 && args[1].equalsIgnoreCase("reset")) {
                    if (!player.hasPermission("cultivar.admin")) {
                        player.sendMessage("§8» §cInsufficient permission.");
                        return true;
                    }
                    if (args.length < 3) {
                        player.sendMessage(
                            "§8» §cUsage: /cv strains reset <player>"
                        );
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        player.sendMessage("§8» §cPlayer not found.");
                        return true;
                    }
                    strainManager.clearPlayerStrains(target.getUniqueId());
                    player.sendMessage(
                        "§8» §7Cleared strain data for §a" +
                            target.getName() +
                            "§7."
                    );
                    return true;
                }
                Set<String> discovered = strainManager.getDiscoveredStrains(
                    player.getUniqueId()
                );
                if (discovered.isEmpty()) {
                    player.sendMessage(
                        "§8» §7No strains discovered in your journal."
                    );
                } else {
                    player.sendMessage("§2§lYour Strains");
                    for (String strainId : discovered) {
                        CropType cropType = strainManager.getStrainCropType(player.getUniqueId(), strainId);
                        StrainProfile strain = StrainProfile.generate(strainId, cropType);
                        String bonuses = "§7";
                        if (cropType == CropType.CANNABIS) {
                            if (strain.yieldBonus > 0.2) bonuses += "High Yield ";
                            if (strain.speedMultiplier < 0.9) bonuses += "Fast ";
                            if (strain.stressResistance > 0) bonuses += "Resilient ";
                        } else if (cropType == CropType.TOBACCO) {
                            if (strain.curabilityBonus > 0.2) bonuses += "High Quality ";
                            if (strain.leafYieldBonus > 0) bonuses += "+" + strain.leafYieldBonus + " Leaves ";
                            bonuses += strain.aromaProfile + " ";
                        } else if (cropType == CropType.TEA) {
                            if (strain.brewStrength > 0.2) bonuses += "Strong ";
                            bonuses += strain.rarityTag + " ";
                        } else if (cropType == CropType.MUSHROOM) {
                            if (strain.potencyLevel > 0) bonuses += "Potency +" + strain.potencyLevel + " ";
                            if (strain.lightTolerance > 0) bonuses += "Light Tol +" + strain.lightTolerance + " ";
                            bonuses += "Spores +" + strain.sporeDensity + " ";
                        }
                        if (bonuses.equals("§7")) bonuses = "§7Balanced";
                        player.sendMessage(
                            " §8• §a" + strain.name + " §8[§7" + cropType.name() + "§8] §8- " + bonuses
                        );
                    }
                }
            }
            case "rename" -> {
                ItemStack held = player.getInventory().getItemInMainHand();
                if (held == null || held.getType() == Material.AIR) {
                    player.sendMessage("§8» §cHold a seed to rename.");
                    return true;
                }
                
                String strainId = ItemFactory.getStrainId(held);
                String strainName = ItemFactory.getStrainName(held);
                if (strainId == null) {
                    player.sendMessage("§8» §cThis item has no strain.");
                    return true;
                }
                
                String breederUuid = ItemFactory.getOriginalBreeder(held);
                if (breederUuid == null || !breederUuid.equals(player.getUniqueId().toString())) {
                    player.sendMessage("§8» §cYou can only rename strains you created.");
                    return true;
                }
                
                if (args.length < 2) {
                    player.sendMessage("§8» §cUsage: /cv rename <name>");
                    return true;
                }
                
                String newName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                if (newName.length() > 30) {
                    player.sendMessage("§8» §cName too long (max 30 characters).");
                    return true;
                }
                
                ItemMeta meta = held.getItemMeta();
                if (meta != null) {
                    meta.getPersistentDataContainer().set(
                        new NamespacedKey("cultivar", "strain_name"),
                        PersistentDataType.STRING,
                        newName
                    );
                    
                    List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                    boolean foundName = false;
                    for (int i = 0; i < lore.size(); i++) {
                        if (lore.get(i).contains("[")) {
                            lore.set(i, "§7[" + newName + "]");
                            foundName = true;
                            break;
                        }
                    }
                    if (!foundName && strainName != null) {
                        String oldDisplay = "[" + strainName + "]";
                        for (int i = 0; i < lore.size(); i++) {
                            if (lore.get(i).contains(oldDisplay)) {
                                lore.set(i, lore.get(i).replace(oldDisplay, "[" + newName + "]"));
                                break;
                            }
                        }
                    }
                    meta.setLore(lore);
                    
                    String displayName = held.getType().name().contains("SEEDS") ? "§2Cannabis Seed" : 
                        (held.getType().name().contains("BROWN") || held.getType().name().contains("RED") ? "§dMushroom Spore" :
                        (held.getType().name().contains("SWEET") ? "§6Tobacco Seed" : "§bTea Seed"));
                    if (displayName.contains("Cannabis")) {
                        meta.setDisplayName("§2Cannabis Seed §7[" + newName + "]");
                    } else if (displayName.contains("Mushroom")) {
                        meta.setDisplayName("§dMushroom Spore §7[" + newName + "]");
                    } else if (displayName.contains("Tobacco")) {
                        meta.setDisplayName("§6Tobacco Seed §7[" + newName + "]");
                    } else {
                        meta.setDisplayName("§bTea Seed §7[" + newName + "]");
                    }
                    
                    held.setItemMeta(meta);
                    player.sendMessage("§8» §aStrain renamed to §7" + newName + "§a!");
                }
                return true;
            }
            case "journal" -> {
                if (strainManager == null) {
                    animator.reveal(
                        player,
                        "§cStrain manager not available",
                        null
                    );
                    return true;
                }
                strainManager.loadPlayerStrains(player.getUniqueId());
                Set<String> discovered = strainManager.getDiscoveredStrains(
                    player.getUniqueId()
                );
                if (discovered.isEmpty()) {
                    animator.reveal(
                        player,
                        "§7No strains discovered yet",
                        null
                    );
                    return true;
                }

                org.bukkit.inventory.ItemStack book =
                    new org.bukkit.inventory.ItemStack(
                        org.bukkit.Material.WRITTEN_BOOK
                    );
                org.bukkit.inventory.meta.BookMeta meta =
                    (org.bukkit.inventory.meta.BookMeta) book.getItemMeta();
                if (meta == null) {
                    animator.reveal(player, "§cError creating journal", null);
                    return true;
                }

                meta.setTitle("Strain Journal");
                meta.setAuthor("Cultivar");

                List<String> pages = new ArrayList<>();

                StringBuilder page1 = new StringBuilder();
                page1.append("§6=== Strain Journal ===\n\n");
                page1
                    .append("§7Discovered: ")
                    .append(discovered.size())
                    .append("\n\n");
                page1.append(
                    "§8Use §a/cv journal§8 to view your discovered strains."
                );
                pages.add(page1.toString());

                for (String strainId : discovered) {
                    if (strainId == null) continue;
                    CropType cropType = strainManager.getStrainCropType(player.getUniqueId(), strainId);
                    StrainProfile strain = StrainProfile.generate(strainId, cropType);
                    if (strain == null) continue;

                    StringBuilder page = new StringBuilder();
                    page.append("§6").append(strain.name).append("\n");
                    page.append("§7Type: ").append(cropType.name()).append("\n\n");
                    
                    if (cropType == CropType.CANNABIS) {
                        page.append("§7Potency: ").append(strain.potency).append("\n");
                        int yieldPct = (int) Math.round(strain.yieldBonus * 100);
                        int speedPct = (int) Math.round(strain.speedMultiplier * 100);
                        page.append("§eYield: +").append(yieldPct).append("%  §bSpeed: ").append(speedPct).append("%\n");
                        page.append("§2Resilience: ").append(strain.stressResistance).append("\n\n");
                    } else if (cropType == CropType.TOBACCO) {
                        int qualityPct = (int) Math.round(strain.curabilityBonus * 100);
                        page.append("§7Quality: +").append(qualityPct).append("%\n");
                        page.append("§7Leafs: +").append(strain.leafYieldBonus).append("\n");
                        page.append("§7Aroma: ").append(strain.aromaProfile).append("\n\n");
                    } else if (cropType == CropType.TEA) {
                        int strengthPct = (int) Math.round(strain.brewStrength * 100);
                        page.append("§7Strength: +").append(strengthPct).append("%\n");
                        page.append("§7Rarity: ").append(strain.rarityTag).append("\n\n");
                    } else if (cropType == CropType.MUSHROOM) {
                        page.append("§7Potency: +").append(strain.potencyLevel).append("\n");
                        page.append("§7Light Tol: +").append(strain.lightTolerance).append("\n");
                        page.append("§7Spores: +").append(strain.sporeDensity).append("\n\n");
                    }

                    String flavor = getStrainFlavorLine(strain);
                    page.append("§8\"").append(flavor).append("\"");

                    pages.add(page.toString());
                }

                meta.setPages(pages);
                book.setItemMeta(meta);

                player.openBook(book);
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
                        player.sendMessage(
                            "§8» §7Soil enrichment set to: §a" + level
                        );
                    }
                    case "clear" -> {
                        soilManager.setEnrichment(targetBlock.getLocation(), 0);
                        player.sendMessage("§8» §aSoil enrichment cleared.");
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

    private String getStrainFlavorLine(StrainProfile strain) {
        if (strain.cropType == CropType.CANNABIS && strain.potency != null) {
            return switch (strain.potency) {
                case "High Yield" -> "A generous plant, grown in patience.";
                case "Fast" -> "Quick to grow, light on the tongue.";
                case "Resilient" -> "Sturdy and steadfast through any weather.";
                case "Premium" -> "A rare find, worth the wait.";
                case "Elite" -> "The pinnacle of cultivation.";
                case "Balanced" -> "A well-rounded choice for any occasion.";
                default -> "An intriguing specimen with unique character.";
            };
        }
        return switch (strain.cropType) {
            case TOBACCO -> switch (strain.aromaProfile) {
                case "sweet" -> "Sweet and smooth on the inhale.";
                case "smoky" -> "Bold and rich with depth.";
                case "earthy" -> "Grounded and natural flavor.";
                case "mild" -> "Gentle and easy-going.";
                case "robust" -> "Strong character, full-bodied.";
                default -> "A unique tobacco varietal.";
            };
            case TEA -> switch (strain.rarityTag) {
                case "aged" -> "Aged to perfection, deep and complex.";
                case "rare" -> "A rare and precious cultivar.";
                default -> "A refreshing and calming brew.";
            };
            case MUSHROOM -> "Potent and earthy, nature's gift.";
            default -> "An intriguing specimen with unique character.";
        };
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
                    return Arrays.asList("list", "info", "give", "remove", "reset");
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
            if ("strains".equals(args[0].toLowerCase()) && "list".equals(args[1].toLowerCase())) {
                return Bukkit.getOnlinePlayers().stream()
                    .map(p -> p.getName())
                    .collect(Collectors.toList());
            }
            if (!sender.hasPermission("cultivar.admin")) return empty;
            if ("strains".equals(args[0].toLowerCase())) {
                if ("info".equals(args[1].toLowerCase()) || "give".equals(args[1].toLowerCase()) || "remove".equals(args[1].toLowerCase())) {
                    strainManager.loadPlayerStrains(((Player)sender).getUniqueId());
                    Set<String> strains = strainManager.getDiscoveredStrains(((Player)sender).getUniqueId());
                    return new ArrayList<>(strains);
                }
            }
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
                        "cannabis_seed:",
                        "tobacco_seed:",
                        "tea_seed:",
                        "mushroom_seed:",
                        "light_cured_leaf",
                        "dark_cured_leaf",
                        "fire_cured_leaf",
                        "tea_bottle:green",
                        "tea_bottle:black",
                        "tea_bottle:herbal"
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
        if (args.length == 4) {
            if (!sender.hasPermission("cultivar.admin")) return empty;
            if ("strains".equals(args[0].toLowerCase()) && "give".equals(args[1].toLowerCase())) {
                return Arrays.asList("CANNABIS", "TOBACCO", "TEA", "MUSHROOM");
            }
            if ("give".equals(args[0].toLowerCase())) {
                if (args[2].startsWith("cannabis_seed:")) {
                    strainManager.loadPlayerStrains(((Player)sender).getUniqueId());
                    Set<String> strains = strainManager.getDiscoveredStrains(((Player)sender).getUniqueId());
                    return new ArrayList<>(strains);
                }
                if (args[2].startsWith("tobacco_seed:")) {
                    strainManager.loadPlayerStrains(((Player)sender).getUniqueId());
                    Set<String> strains = strainManager.getDiscoveredStrains(((Player)sender).getUniqueId());
                    return new ArrayList<>(strains);
                }
                if (args[2].startsWith("tea_seed:")) {
                    strainManager.loadPlayerStrains(((Player)sender).getUniqueId());
                    Set<String> strains = strainManager.getDiscoveredStrains(((Player)sender).getUniqueId());
                    return new ArrayList<>(strains);
                }
                if (args[2].startsWith("mushroom_seed:")) {
                    strainManager.loadPlayerStrains(((Player)sender).getUniqueId());
                    Set<String> strains = strainManager.getDiscoveredStrains(((Player)sender).getUniqueId());
                    return new ArrayList<>(strains);
                }
            }
        }
        return empty;
    }
}
