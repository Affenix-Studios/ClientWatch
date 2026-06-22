package com.clientwatch.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class MessageService {
    private final JavaPlugin plugin;
    private FileConfiguration messages;

    public MessageService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
    }

    public String get(String key) {
        String raw = messages.getString(key, key);
        return color(messages.getString("prefix", "") + raw);
    }

    public String raw(String key) {
        return color(messages.getString(key, key));
    }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
