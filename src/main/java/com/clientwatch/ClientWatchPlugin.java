package com.clientwatch;

import com.clientwatch.api.ClientWatchApi;
import com.clientwatch.command.ClientWatchCommand;
import com.clientwatch.config.MessageService;
import com.clientwatch.detection.ClientDetectionService;
import com.clientwatch.detection.JoinListener;
import com.clientwatch.detection.PreJoinListener;
import com.clientwatch.service.ActionService;
import com.clientwatch.service.BlacklistService;
import com.clientwatch.service.DetectionLogService;
import com.clientwatch.service.DiscordWebhookService;
import com.clientwatch.service.ExportService;
import com.clientwatch.service.UpdateChecker;
import com.clientwatch.storage.DetectionRepository;
import com.clientwatch.storage.InMemoryDetectionRepository;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClientWatchPlugin extends JavaPlugin {
    private MessageService messages;
    private BlacklistService blacklistService;
    private ClientDetectionService detectionService;
    private UpdateChecker updateChecker;
    private ClientWatchApi api;

    @Override
    public void onEnable() {
        // Ensure config loads safely and recover from malformed YAML
        try {
            saveDefaultConfig();
            reloadConfig();
        } catch (Exception e) {
            getLogger().warning("Could not load config.yml: " + e.getMessage());
            try {
                java.nio.file.Path cfg = getDataFolder().toPath().resolve("config.yml");
                if (java.nio.file.Files.exists(cfg)) {
                    java.nio.file.Path backup = getDataFolder().toPath().resolve("config.yml.broken." + System.currentTimeMillis());
                    java.nio.file.Files.move(cfg, backup);
                    getLogger().warning("Backed up broken config to " + backup.getFileName());
                }
                saveResource("config.yml", false);
                reloadConfig();
            } catch (Exception ex) {
                getLogger().severe("Failed to recover config.yml: " + ex.getMessage());
            }
        }

        messages = new MessageService(this);
        try {
            messages.reload();
        } catch (Exception e) {
            getLogger().warning("Could not load messages.yml: " + e.getMessage());
        }

        blacklistService = new BlacklistService();
        blacklistService.reload(getConfig());

        DetectionRepository repository = new InMemoryDetectionRepository();
        DiscordWebhookService discordWebhookService = new DiscordWebhookService(this);
        ActionService actionService = new ActionService(this, messages, discordWebhookService);
        DetectionLogService logService = new DetectionLogService(this);
        ExportService exportService = new ExportService(this);
        api = new ClientWatchApi(repository, exportService);
        detectionService = new ClientDetectionService(this, repository, blacklistService, actionService, logService);
        detectionService.register();
        getServer().getPluginManager().registerEvents(new PreJoinListener(detectionService, blacklistService, messages), this);
        getServer().getPluginManager().registerEvents(new JoinListener(detectionService), this);

        updateChecker = new UpdateChecker(this);
        updateChecker.start();
        new Metrics(this, 32153);

        ClientWatchCommand executor = new ClientWatchCommand(this, messages, repository, updateChecker, blacklistService, exportService);
        PluginCommand command = getCommand("clientwatch");
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
        getLogger().info("ClientWatch enabled.");
    }

    public ClientWatchApi api() {
        return api;
    }

    public boolean isDebugEnabled() {
        return getConfig().getBoolean("debug", false);
    }

    @Override
    public void onDisable() {
        if (detectionService != null) {
            detectionService.unregister();
        }
        getLogger().info("ClientWatch disabled.");
    }
}
