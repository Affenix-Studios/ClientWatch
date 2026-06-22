package com.clientwatch.service;

import com.clientwatch.model.ClientDetection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

public final class DetectionLogService {
    private final JavaPlugin plugin;

    public DetectionLogService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void write(ClientDetection detection) {
        if (!plugin.getConfig().getBoolean("logging.enabled", true)) {
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Path directory = plugin.getDataFolder().toPath().resolve("logs");
                Files.createDirectories(directory);
                Path file = directory.resolve(LocalDate.now() + ".log");
                String line = "%s | version=%s | player=%s | uuid=%s | client=%s | loader=%s | mods=%d | matches=%d | durationMs=%d%n".formatted(
                    detection.detectedAt(),
                    plugin.getDescription().getVersion(),
                    detection.playerName(),
                    detection.uuid(),
                    detection.clientBrand(),
                    detection.loader(),
                    detection.modCount(),
                    detection.blacklistMatches().size(),
                    detection.detectionDurationMillis()
                );
                Files.writeString(file, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException exception) {
                plugin.getLogger().warning("Could not write ClientWatch detection log: " + exception.getMessage());
            }
        });
    }
}
