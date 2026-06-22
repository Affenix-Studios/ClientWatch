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
        File file = new File(plugin.getDataFolder(), "messages.yml");
        try {
            messages = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not load messages.yml: " + e.getMessage());
            try {
                File backup = new File(plugin.getDataFolder(), "messages.yml.broken." + System.currentTimeMillis());
                if (file.exists()) file.renameTo(backup);
                plugin.saveResource("messages.yml", false);
                messages = YamlConfiguration.loadConfiguration(file);
                plugin.getLogger().warning("Replaced messages.yml with default and backed up broken file to " + backup.getName());
            } catch (Exception ex) {
                plugin.getLogger().severe("Failed to recover messages.yml: " + ex.getMessage());
                messages = YamlConfiguration.loadConfiguration(file);
            }
        }
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
