package com.clientwatch.detection;

import com.clientwatch.api.event.PlayerBlacklistedModEvent;
import com.clientwatch.api.event.PlayerClientDetectEvent;
import com.clientwatch.model.ClientDetection;
import com.clientwatch.model.ClientLoader;
import com.clientwatch.model.ModInfo;
import com.clientwatch.service.ActionService;
import com.clientwatch.service.BlacklistService;
import com.clientwatch.service.DetectionLogService;
import com.clientwatch.storage.DetectionRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientDetectionService implements PluginMessageListener {
    private static final String BRAND_CHANNEL = "minecraft:brand";

    private final JavaPlugin plugin;
    private final DetectionRepository repository;
    private final BlacklistService blacklistService;
    private final ActionService actionService;
    private final DetectionLogService logService;
    private final LoaderResolver loaderResolver = new LoaderResolver();
    private final DetectionAnalyzer analyzer = new DetectionAnalyzer();
    private final Map<UUID, String> brands = new ConcurrentHashMap<>();

    public ClientDetectionService(JavaPlugin plugin, DetectionRepository repository, BlacklistService blacklistService, ActionService actionService, DetectionLogService logService) {
        this.plugin = plugin;
        this.repository = repository;
        this.blacklistService = blacklistService;
        this.actionService = actionService;
        this.logService = logService;
    }

    public void register() {
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, BRAND_CHANNEL, this);
    }

    public void unregister() {
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, BRAND_CHANNEL, this);
    }

    public void detect(Player player) {
        long delay = Math.max(20L, plugin.getConfig().getLong("detection.brand-timeout-seconds", 8L) * 20L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> saveDetection(player), delay);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!BRAND_CHANNEL.equals(channel)) {
            return;
        }
        // store brand from plugin message and schedule save on the main thread
        brands.put(player.getUniqueId(), decodeBrand(message));
        Bukkit.getScheduler().runTask(plugin, () -> saveDetection(player));
    }

    private void saveDetection(Player player) {
        if (!player.isOnline()) {
            return;
        }
        long startedAt = System.nanoTime();
        String brand = resolveBrand(player);
        List<ModInfo> mods = List.of();
        ClientLoader loader = loaderResolver.resolve(brand, mods);
        List<String> warnings = analyzer.analyze(brand, mods);
        Instant detectedAt = Instant.now();
        long durationMillis = Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
        ClientDetection base = new ClientDetection(
            player.getUniqueId(),
            player.getName(),
            player.getProtocolVersion() > 0 ? "Protocol " + player.getProtocolVersion() : "Unknown",
            player.getProtocolVersion(),
            brand,
            brand,
            loader,
            mods,
            Instant.now(),
            player.getPing(),
            Bukkit.getOnlineMode(),
            Bukkit.getName(),
            player.getAddress() == null ? null : player.getAddress().getAddress(),
            detectedAt,
            durationMillis,
            warnings,
            List.of()
        );
        List<String> matches = blacklistService.findMatches(base);
        ClientDetection detection = new ClientDetection(
            base.uuid(), base.playerName(), base.minecraftVersion(), base.protocolVersion(), base.clientBrand(),
            base.clientName(), base.loader(), base.mods(), base.joinTime(), base.ping(), base.onlineMode(),
            base.serverBrand(), base.address(), base.detectedAt(), base.detectionDurationMillis(), base.warnings(), matches
        );
        repository.save(detection).thenRun(() -> {
            Bukkit.getPluginManager().callEvent(new PlayerClientDetectEvent(detection, true));
            logService.write(detection);
        });
        if (!matches.isEmpty()) {
            Bukkit.getPluginManager().callEvent(new PlayerBlacklistedModEvent(detection, true));
            actionService.handleBlacklistMatch(detection);
        }
    }

    private String decodeBrand(byte[] message) {
        if (message.length == 0) {
            return "Unknown";
        }
        VarInt stringLength = readVarInt(message);
        // bytesRead < 0 indicates an invalid VarInt
        if (stringLength.bytesRead() < 0) {
            return "Unknown";
        }
        int start = stringLength.bytesRead();
        int safeLength = Math.min(stringLength.value(), Math.max(0, message.length - start));
        String decoded = new String(message, start, safeLength, StandardCharsets.UTF_8).trim();
        return decoded.isBlank() ? "Unknown" : decoded;
    }

    private String resolveBrand(Player player) {
        // Prefer Paper API when available, fallback to plugin message
        String paperBrand = null;
        try {
            paperBrand = player.getClientBrandName();
        } catch (NoSuchMethodError | UnsupportedOperationException ignored) {
            // older servers may not expose this method
        }
        if (isKnown(paperBrand)) {
            brands.put(player.getUniqueId(), paperBrand);
            return paperBrand;
        }
        String pluginMessageBrand = brands.get(player.getUniqueId());
        if (isKnown(pluginMessageBrand)) {
            return pluginMessageBrand;
        }
        return "Unknown";
    }

    private boolean isKnown(String value) {
        return value != null && !value.isBlank() && !value.equalsIgnoreCase("Unknown");
    }

    private VarInt readVarInt(byte[] bytes) {
        int value = 0;
        int position = 0;
        for (int index = 0; index < Math.min(bytes.length, 5); index++) {
            int current = bytes[index] & 0xFF;
            value |= (current & 0x7F) << position;
            if ((current & 0x80) == 0) {
                return new VarInt(value, index + 1);
            }
            position += 7;
        }
        return new VarInt(0, -1);
    }

    private record VarInt(int value, int bytesRead) {
    }
}
