package com.timuzkas.cultivar;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ActionBarAnimator {
    private final Plugin plugin;
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    public ActionBarAnimator(Plugin plugin) {
        this.plugin = plugin;
    }

    public void reveal(Player player, String message, Consumer<Player> onComplete) {
        cancelTask(player);
        RevealTask task = new RevealTask(player, message, onComplete);
        activeTasks.put(player.getUniqueId(), task.runTaskTimer(plugin, 0, 1));
    }

    public void idle(Player player, String message) {
        cancelTask(player);
        IdleTask task = new IdleTask(player, message);
        activeTasks.put(player.getUniqueId(), task.runTaskTimer(plugin, 0, 10));
    }

    public void instant(Player player, String message) {
        cancelTask(player);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    public void sequence(Player player, List<String> lines, int ticksPerLine) {
        cancelTask(player);
        SequenceTask task = new SequenceTask(player, lines, ticksPerLine);
        activeTasks.put(player.getUniqueId(), task.runTaskTimer(plugin, 0, 1));
    }

    public void clear(Player player) {
        cancelTask(player);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
    }

    public void clearAll() {
        for (UUID uuid : activeTasks.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
            }
        }
        activeTasks.values().forEach(BukkitTask::cancel);
        activeTasks.clear();
    }

    private void cancelTask(Player player) {
        BukkitTask task = activeTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    private static class RevealTask extends BukkitRunnable {
        private final Player player;
        private final String message;
        private final Consumer<Player> onComplete;
        private int index = 0;

        public RevealTask(Player player, String message, Consumer<Player> onComplete) {
            this.player = player;
            this.message = message;
            this.onComplete = onComplete;
        }

        @Override
        public void run() {
            if (index < message.length()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message.substring(0, index + 1)));
                index++;
            } else {
                if (onComplete != null) {
                    onComplete.accept(player);
                }
                cancel();
            }
        }
    }

    private static class IdleTask extends BukkitRunnable {
        private final Player player;
        private final String message;
        private boolean blink = true;

        public IdleTask(Player player, String message) {
            this.player = player;
            this.message = message;
        }

        @Override
        public void run() {
            String display = message + (blink ? "§7▌" : "");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(display));
            blink = !blink;
        }
    }

    private static class SequenceTask extends BukkitRunnable {
        private final Player player;
        private final List<String> lines;
        private final int ticksPerLine;
        private int currentLine = 0;
        private int ticksOnLine = 0;

        public SequenceTask(Player player, List<String> lines, int ticksPerLine) {
            this.player = player;
            this.lines = lines;
            this.ticksPerLine = ticksPerLine;
        }

        @Override
        public void run() {
            if (currentLine >= lines.size()) {
                cancel();
                return;
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(lines.get(currentLine)));
            ticksOnLine++;
            if (ticksOnLine >= ticksPerLine) {
                currentLine++;
                ticksOnLine = 0;
            }
        }
    }
}