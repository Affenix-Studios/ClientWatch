package com.clientwatch.service;

import com.clientwatch.model.ClientDetection;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class DiscordWebhookService {
    private final JavaPlugin plugin;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public DiscordWebhookService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendBlacklistAlert(ClientDetection detection, String executedAction) {
        if (!plugin.getConfig().getBoolean("discord.enabled", false)) {
            return;
        }
        String webhookUrl = plugin.getConfig().getString("discord.webhook-url", "");
        if (webhookUrl.isBlank()) {
            return;
        }
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl))
            .timeout(Duration.ofSeconds(8))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload(detection, executedAction)))
            .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .exceptionally(error -> {
                plugin.getLogger().warning("Could not send ClientWatch Discord webhook: " + error.getMessage());
                return null;
            });
    }

    private String payload(ClientDetection detection, String executedAction) {
        int color = plugin.getConfig().getInt("discord.severity-colors.high", 15548997);
        String avatar = "https://mc-heads.net/avatar/" + detection.uuid();
        String mods = detection.mods().isEmpty() ? "Unavailable" : String.valueOf(detection.modCount());
        return """
            {
              "embeds": [{
                "title": "ClientWatch blacklist match",
                "color": %d,
                "thumbnail": {"url": "%s"},
                "timestamp": "%s",
                "fields": [
                  {"name": "Server", "value": "%s", "inline": true},
                  {"name": "Plugin Version", "value": "%s", "inline": true},
                  {"name": "Player", "value": "%s", "inline": true},
                  {"name": "UUID", "value": "%s", "inline": false},
                  {"name": "Client", "value": "%s", "inline": true},
                  {"name": "Loader", "value": "%s", "inline": true},
                  {"name": "Detected Mods", "value": "%s", "inline": true},
                  {"name": "Executed Action", "value": "%s", "inline": false},
                  {"name": "Matches", "value": "%s", "inline": false}
                ]
              }]
            }
            """.formatted(
            color,
            escape(avatar),
            detection.detectedAt(),
            escape(Bukkit.getServer().getName()),
            escape(plugin.getDescription().getVersion()),
            escape(detection.playerName()),
            detection.uuid(),
            escape(detection.clientBrand()),
            detection.loader(),
            escape(mods),
            escape(executedAction),
            escape(String.join(", ", detection.blacklistMatches()))
        );
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
