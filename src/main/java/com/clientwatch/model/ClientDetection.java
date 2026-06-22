package com.clientwatch.model;

import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ClientDetection(
    UUID uuid,
    String playerName,
    String minecraftVersion,
    int protocolVersion,
    String clientBrand,
    String clientName,
    ClientLoader loader,
    List<ModInfo> mods,
    Instant joinTime,
    int ping,
    boolean onlineMode,
    String serverBrand,
    InetAddress address,
    Instant detectedAt,
    long detectionDurationMillis,
    List<String> warnings,
    List<String> blacklistMatches
) {
    public int modCount() {
        return mods.size();
    }
}
