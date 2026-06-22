package com.clientwatch.storage;

import com.clientwatch.model.ClientDetection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InMemoryDetectionRepository implements DetectionRepository {
    private final Map<UUID, List<ClientDetection>> detections = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Void> save(ClientDetection detection) {
        detections.computeIfAbsent(detection.uuid(), ignored -> new CopyOnWriteArrayList<>()).add(detection);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Optional<ClientDetection> latest(UUID uuid) {
        return detections.getOrDefault(uuid, List.of()).stream().max(Comparator.comparing(ClientDetection::detectedAt));
    }

    @Override
    public Optional<ClientDetection> latest(String playerName) {
        return detections.values().stream()
            .flatMap(List::stream)
            .filter(detection -> detection.playerName().equalsIgnoreCase(playerName))
            .max(Comparator.comparing(ClientDetection::detectedAt));
    }

    @Override
    public List<ClientDetection> history(UUID uuid) {
        return List.copyOf(detections.getOrDefault(uuid, List.of()));
    }

    @Override
    public List<ClientDetection> latestOnline() {
        return detections.keySet().stream().map(this::latest).flatMap(Optional::stream).toList();
    }

    @Override
    public List<ClientDetection> searchByMod(String query) {
        return latestOnline().stream()
            .filter(detection -> detection.mods().stream().anyMatch(mod -> mod.matches(query)))
            .toList();
    }
}
