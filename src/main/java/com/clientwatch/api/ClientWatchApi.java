package com.clientwatch.api;

import com.clientwatch.model.ClientDetection;
import com.clientwatch.service.ExportService;
import com.clientwatch.storage.DetectionRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ClientWatchApi {
    private final DetectionRepository repository;
    private final ExportService exportService;

    public ClientWatchApi(DetectionRepository repository, ExportService exportService) {
        this.repository = repository;
        this.exportService = exportService;
    }

    public Optional<ClientDetection> latest(UUID uuid) {
        return repository.latest(uuid);
    }

    public Optional<ClientDetection> latest(String playerName) {
        return repository.latest(playerName);
    }

    public List<ClientDetection> onlineDetections() {
        return repository.latestOnline();
    }

    public List<ClientDetection> findPlayersWithMod(String query) {
        return repository.searchByMod(query);
    }

    public Path export(ClientDetection detection) throws IOException {
        return exportService.export(detection);
    }
}
