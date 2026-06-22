package com.clientwatch.service;

import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class UpdateChecker {
    private final JavaPlugin plugin;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private volatile String latestMessage;

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("update-checker.enabled", true)) {
            return;
        }
        checkNow();
        long interval = plugin.getConfig().getLong("update-checker.interval-hours", 6L) * 60L * 60L * 20L;
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::checkNow, interval, interval);
    }

    public CompletableFuture<Optional<String>> checkNow() {
        String project = plugin.getConfig().getString("update-checker.modrinth-project", "clientwatch");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.modrinth.com/v2/project/" + project + "/version"))
            .timeout(Duration.ofSeconds(8))
            .header("User-Agent", "ClientWatch/" + plugin.getDescription().getVersion())
            .GET()
            .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    latestMessage = "Update check completed. Open the Modrinth project page for changelog details.";
                    plugin.getLogger().info(latestMessage);
                    return Optional.of(latestMessage);
                }
                latestMessage = null;
                return Optional.<String>empty();
            })
            .exceptionally(error -> Optional.empty());
    }

    public Optional<String> latestMessage() {
        return Optional.ofNullable(latestMessage);
    }
}
