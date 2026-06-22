package com.clientwatch.storage;

import com.clientwatch.model.ClientDetection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DetectionRepository {
    CompletableFuture<Void> save(ClientDetection detection);
    Optional<ClientDetection> latest(UUID uuid);
    Optional<ClientDetection> latest(String playerName);
    List<ClientDetection> history(UUID uuid);
    List<ClientDetection> latestOnline();
    List<ClientDetection> searchByMod(String query);
}
