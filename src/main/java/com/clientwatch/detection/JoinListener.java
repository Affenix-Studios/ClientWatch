package com.clientwatch.detection;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class JoinListener implements Listener {
    private final ClientDetectionService detectionService;

    public JoinListener(ClientDetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        detectionService.detect(event.getPlayer());
    }
}
