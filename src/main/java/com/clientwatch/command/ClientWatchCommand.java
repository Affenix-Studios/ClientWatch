package com.clientwatch.command;

import com.clientwatch.api.event.PlayerInspectionEvent;
import com.clientwatch.config.MessageService;
import com.clientwatch.model.ClientDetection;
import com.clientwatch.model.ModInfo;
import com.clientwatch.service.BlacklistService;
import com.clientwatch.service.ExportService;
import com.clientwatch.service.UpdateChecker;
import com.clientwatch.storage.DetectionRepository;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class ClientWatchCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final MessageService messages;
    private final DetectionRepository repository;
    private final UpdateChecker updateChecker;
    private final BlacklistService blacklistService;
    private final ExportService exportService;

    public ClientWatchCommand(JavaPlugin plugin, MessageService messages, DetectionRepository repository, UpdateChecker updateChecker, BlacklistService blacklistService, ExportService exportService) {
        this.plugin = plugin;
        this.messages = messages;
        this.repository = repository;
        this.updateChecker = updateChecker;
        this.blacklistService = blacklistService;
        this.exportService = exportService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messages.color("&bClientWatch &7- reliable client inspection for Paper servers."));
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("reload")) {
            if (!require(sender, "clientwatch.admin")) {
                return true;
            }
            plugin.reloadConfig();
            messages.reload();
            blacklistService.reload(plugin.getConfig());
            sender.sendMessage(messages.get("reload"));
            return true;
        }
        if (sub.equals("version")) {
            sender.sendMessage(messages.get("version").replace("{version}", plugin.getDescription().getVersion()));
            return true;
        }
        if (sub.equals("update")) {
            updateChecker.checkNow().thenAccept(result -> sender.sendMessage(result.orElse(messages.get("update-unavailable"))));
            return true;
        }
        if (sub.equals("alerts")) {
            sender.sendMessage(messages.raw("alerts-enabled"));
            return true;
        }
        if (sub.equals("online")) {
            if (!require(sender, "clientwatch.inspect")) {
                return true;
            }
            sendOnline(sender);
            return true;
        }
        if (sub.equals("search")) {
            if (!require(sender, "clientwatch.inspect")) {
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(messages.color("&cUsage: /clientwatch search <mod>"));
                return true;
            }
            sendSearch(sender, args[1]);
            return true;
        }
        if (sub.equals("export")) {
            if (!require(sender, "clientwatch.inspect")) {
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(messages.color("&cUsage: /clientwatch export <player>"));
                return true;
            }
            export(sender, args[1]);
            return true;
        }
        if (sub.equals("inspect") || sub.equals("info") || sub.equals("mods") || sub.equals("history")) {
            if (!sender.hasPermission("clientwatch.inspect")) {
                sender.sendMessage(messages.get("no-permission"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(messages.color("&cUsage: /clientwatch " + sub + " <player>"));
                return true;
            }
            Optional<ClientDetection> detection = repository.latest(args[1]);
            if (detection.isEmpty()) {
                sender.sendMessage(messages.get("player-not-found"));
                return true;
            }
            if (sub.equals("mods")) {
                sendMods(sender, detection.get());
            } else if (sub.equals("history")) {
                sendHistory(sender, detection.get());
            } else {
                Bukkit.getPluginManager().callEvent(new PlayerInspectionEvent(detection.get(), false));
                sendInspection(sender, detection.get());
            }
            return true;
        }
        if (sub.equals("blacklist") || sub.equals("whitelist")) {
            sender.sendMessage(messages.color("&7Edit blacklist and whitelist entries in config.yml, then run &b/clientwatch reload&7."));
            return true;
        }
        sender.sendMessage(messages.color("&cUnknown ClientWatch command."));
        return true;
    }

    private boolean require(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(messages.get("no-permission"));
            return false;
        }
        return true;
    }

    private void sendInspection(CommandSender sender, ClientDetection detection) {
        sender.sendMessage(messages.raw("inspection-header").replace("{player}", detection.playerName()));
        sender.sendMessage(messages.color("&7Client: &f" + detection.clientName()));
        sender.sendMessage(messages.color("&7Brand: &f" + detection.clientBrand()));
        sender.sendMessage(messages.color("&7Loader: &f" + detection.loader()));
        sender.sendMessage(messages.color("&7Minecraft Version: &f" + detection.minecraftVersion()));
        sender.sendMessage(messages.color("&7Protocol: &f" + detection.protocolVersion()));
        sender.sendMessage(messages.color("&7Ping: &f" + detection.ping() + " ms"));
        sender.sendMessage(messages.color("&7Mods: &f" + detection.modCount()));
        sender.sendMessage(messages.color("&7Blacklist Matches: &f" + String.join(", ", detection.blacklistMatches())));
        detection.warnings().forEach(warning -> sender.sendMessage(messages.color("&e" + warning)));
    }

    private void sendMods(CommandSender sender, ClientDetection detection) {
        sender.sendMessage(messages.raw("mods-header").replace("{player}", detection.playerName()));
        if (detection.mods().isEmpty()) {
            sender.sendMessage(messages.get("no-mods"));
            return;
        }
        for (ModInfo mod : detection.mods()) {
            sender.sendMessage(messages.color("&7- &f" + mod.name() + " &8(" + mod.id() + ", " + mod.version() + ")"));
        }
    }

    private void sendHistory(CommandSender sender, ClientDetection detection) {
        repository.history(detection.uuid()).forEach(entry -> sender.sendMessage(messages.color(
            "&7" + entry.detectedAt() + " &f" + entry.clientBrand() + " &8/ &f" + entry.loader()
        )));
    }

    private void sendOnline(CommandSender sender) {
        sender.sendMessage(messages.color("&bOnline ClientWatch players"));
        repository.latestOnline().stream()
            .sorted(Comparator.comparing(ClientDetection::playerName, String.CASE_INSENSITIVE_ORDER))
            .forEach(detection -> sender.sendMessage(messages.color(
                "&7- &f" + detection.playerName() + " &8| &7Client: &f" + detection.clientName()
                    + " &8| &7Loader: &f" + detection.loader() + " &8| &7Mods: &f" + detection.modCount()
            )));
    }

    private void sendSearch(CommandSender sender, String query) {
        List<ClientDetection> matches = repository.searchByMod(query);
        sender.sendMessage(messages.color("&bMod search results for &f" + query + "&b: &f" + matches.size()));
        for (ClientDetection detection : matches) {
            sender.sendMessage(messages.color("&7- &f" + detection.playerName() + " &8| &7Mods: &f" + detection.modCount()));
        }
    }

    private void export(CommandSender sender, String playerName) {
        Optional<ClientDetection> detection = repository.latest(playerName);
        if (detection.isEmpty()) {
            sender.sendMessage(messages.get("player-not-found"));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Path file = exportService.export(detection.get());
                sender.sendMessage(messages.color("&aExported ClientWatch data to &f" + file));
            } catch (IOException exception) {
                sender.sendMessage(messages.color("&cCould not export ClientWatch data: " + exception.getMessage()));
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "version", "update", "inspect", "mods", "info", "history", "alerts", "blacklist", "whitelist", "search", "online", "export")
                .stream().filter(value -> value.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        }
        if (args.length == 2 && List.of("inspect", "mods", "info", "history", "export").contains(args[0].toLowerCase(Locale.ROOT))) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        }
        return List.of();
    }
}
