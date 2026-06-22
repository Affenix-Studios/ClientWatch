package com.clientwatch.service;

import com.clientwatch.config.MessageService;
import com.clientwatch.model.ClientDetection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ActionService {
    private final JavaPlugin plugin;
    private final MessageService messages;
    private final DiscordWebhookService discordWebhookService;

    public ActionService(JavaPlugin plugin, MessageService messages, DiscordWebhookService discordWebhookService) {
        this.plugin = plugin;
        this.messages = messages;
        this.discordWebhookService = discordWebhookService;
    }

    public void handleBlacklistMatch(ClientDetection detection) {
        String matches = String.join(", ", detection.blacklistMatches());
        StringBuilder executedActions = new StringBuilder();
        if (plugin.getConfig().getBoolean("actions.console-message.enabled", true)) {
            plugin.getLogger().warning(detection.playerName() + " matched ClientWatch blacklist: " + matches);
            executedActions.append("console-message ");
        }
        if (plugin.getConfig().getBoolean("actions.staff-message.enabled", true)) {
            String alert = messages.get("blacklist-alert")
                .replace("{player}", detection.playerName())
                .replace("{matches}", matches);
            Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.hasPermission("clientwatch.alerts"))
                .forEach(player -> player.sendMessage(alert));
            executedActions.append("staff-message ");
        }
        if (plugin.getConfig().getBoolean("actions.ban.enabled", false)) {
            String command = plugin.getConfig().getString("actions.ban.command", "")
                .replace("{player}", detection.playerName())
                .replace("{matches}", matches);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            executedActions.append("ban-command ");
        }
        if (plugin.getConfig().getBoolean("actions.kick.enabled", true)) {
            Player player = Bukkit.getPlayer(detection.uuid());
            if (player != null) {
                String reason = messages.color(plugin.getConfig().getString("actions.kick.reason", "&cClient not allowed."));
                Bukkit.getScheduler().runTask(plugin, () -> player.kickPlayer(reason));
            }
            executedActions.append("kick ");
        }
        discordWebhookService.sendBlacklistAlert(detection, executedActions.toString().trim());
    }
}
