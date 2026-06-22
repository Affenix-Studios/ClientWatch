package com.clientwatch.detection;

import com.clientwatch.config.MessageService;
import com.clientwatch.model.ClientLoader;
import com.clientwatch.model.ModInfo;
import com.clientwatch.service.BlacklistService;

import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Blocks players at login time if ClientWatch cannot verify the client loader.
 *
 * Note: We only have access to plugin-message derived data after the login sequence has started.
 * Since modhiders may try to spoof/omit handshake data, this listener is intentionally conservative.
 */
public final class PreJoinListener implements Listener {
    private final ClientDetectionService detectionService;

    private final MessageService messages;

    public PreJoinListener(ClientDetectionService detectionService, BlacklistService blacklistService, MessageService messages) {
        this.detectionService = detectionService;
        this.messages = messages;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (event.getResult() != null) {
            return;
        }

        // We can only make a decision based on what we currently know.
        // If we can't resolve the loader yet, and config says to deny, we deny.
        boolean blockUnknownLoader = detectionService.getPlugin().getConfig().getBoolean("block.unknown-loader.enabled", true);
        boolean denyUnknownLoader = detectionService.getPlugin().getConfig().getBoolean("loader-detection.deny-unknown-loader", true);
        boolean denyMissingBrandAndMods = detectionService.getPlugin().getConfig().getBoolean("loader-detection.deny-missing-brand-and-mods", true);

        if (!blockUnknownLoader) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        String brand = detectionService.getBrand(uuid);
        List<ModInfo> mods = detectionService.getMods(uuid);

        ClientLoader loader = detectionService.resolveLoader(brand, mods);

        boolean hasAnyModList = mods != null && !mods.isEmpty();
        boolean hasKnownBrand = brand != null && !brand.isBlank() && !brand.equalsIgnoreCase("unknown");

        boolean shouldDeny = false;
        if (denyUnknownLoader && loader == ClientLoader.UNKNOWN) {
            shouldDeny = true;
        }
        if (denyMissingBrandAndMods && (!hasKnownBrand && !hasAnyModList)) {
            shouldDeny = true;
        }

        if (!shouldDeny) {
            return;
        }

        String reason = detectionService.getPlugin().getConfig().getString("block.unknown-loader.reason", "&cYour client/loader could not be verified. Please use Vanilla/Fabric/Forge clients.");
        event.disallow(PlayerLoginEvent.Result.KICK_BANNED, messages.color(reason));
    }
}

