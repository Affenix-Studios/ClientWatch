package com.clientwatch.service;

import com.clientwatch.model.ClientDetection;
import com.clientwatch.model.ModInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public final class ExportService {
    private final JavaPlugin plugin;

    public ExportService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Path export(ClientDetection detection) throws IOException {
        Path directory = plugin.getDataFolder().toPath().resolve("exports");
        Files.createDirectories(directory);
        Path file = directory.resolve(detection.playerName() + "-" + detection.uuid() + ".json");
        Files.writeString(file, toJson(detection), StandardCharsets.UTF_8);
        return file;
    }

    private String toJson(ClientDetection detection) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "playerName", detection.playerName(), true);
        field(builder, "uuid", detection.uuid().toString(), true);
        field(builder, "clientBrand", detection.clientBrand(), true);
        field(builder, "clientName", detection.clientName(), true);
        field(builder, "loader", detection.loader().name(), true);
        field(builder, "minecraftVersion", detection.minecraftVersion(), true);
        builder.append("  \"protocolVersion\": ").append(detection.protocolVersion()).append(",\n");
        builder.append("  \"ping\": ").append(detection.ping()).append(",\n");
        builder.append("  \"modCount\": ").append(detection.modCount()).append(",\n");
        builder.append("  \"blacklistMatchCount\": ").append(detection.blacklistMatches().size()).append(",\n");
        builder.append("  \"detectionDurationMillis\": ").append(detection.detectionDurationMillis()).append(",\n");
        field(builder, "detectedAt", DateTimeFormatter.ISO_INSTANT.format(detection.detectedAt()), true);
        builder.append("  \"mods\": [");
        for (int index = 0; index < detection.mods().size(); index++) {
            ModInfo mod = detection.mods().get(index);
            builder.append(index == 0 ? "\n" : ",\n")
                .append("    {\"id\":\"").append(escape(mod.id())).append("\",\"name\":\"")
                .append(escape(mod.name())).append("\",\"version\":\"").append(escape(mod.version())).append("\"}");
        }
        builder.append(detection.mods().isEmpty() ? "],\n" : "\n  ],\n");
        array(builder, "warnings", detection.warnings(), true);
        array(builder, "blacklistMatches", detection.blacklistMatches(), false);
        builder.append("}\n");
        return builder.toString();
    }

    private void field(StringBuilder builder, String name, String value, boolean comma) {
        builder.append("  \"").append(name).append("\": \"").append(escape(value)).append("\"");
        builder.append(comma ? ",\n" : "\n");
    }

    private void array(StringBuilder builder, String name, Iterable<String> values, boolean comma) {
        builder.append("  \"").append(name).append("\": [");
        boolean first = true;
        for (String value : values) {
            builder.append(first ? "" : ", ").append("\"").append(escape(value)).append("\"");
            first = false;
        }
        builder.append("]").append(comma ? ",\n" : "\n");
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
