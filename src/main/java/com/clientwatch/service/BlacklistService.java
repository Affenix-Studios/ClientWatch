package com.clientwatch.service;

import com.clientwatch.model.ClientDetection;
import com.clientwatch.model.ModInfo;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BlacklistService {
    private List<String> mods = List.of();
    private List<String> clients = List.of();
    private List<String> loaders = List.of();
    private List<String> whitelistedMods = List.of();
    private List<String> whitelistedClients = List.of();
    private List<String> whitelistedLoaders = List.of();

    public void reload(FileConfiguration config) {
        mods = normalize(config.getStringList("blacklist.mods"));
        clients = normalize(config.getStringList("blacklist.clients"));
        loaders = normalize(config.getStringList("blacklist.loaders"));
        whitelistedMods = normalize(config.getStringList("whitelist.mods"));
        whitelistedClients = normalize(config.getStringList("whitelist.clients"));
        whitelistedLoaders = normalize(config.getStringList("whitelist.loaders"));
    }

    public List<String> findMatches(ClientDetection detection) {
        List<String> matches = new ArrayList<>();
        String clientBrand = normalize(detection.clientBrand());
        String clientName = normalize(detection.clientName());
        String loader = detection.loader().name().toLowerCase(Locale.ROOT);
        if (!whitelistedClients.contains(clientBrand) && (clients.contains(clientBrand) || clients.contains(clientName))) {
            matches.add("client:" + detection.clientBrand());
        }
        if (!whitelistedLoaders.contains(loader) && loaders.contains(loader)) {
            matches.add("loader:" + detection.loader().name());
        }
        for (ModInfo mod : detection.mods()) {
            String id = normalize(mod.id());
            String name = normalize(mod.name());
            if (!whitelistedMods.contains(id) && (mods.contains(id) || mods.contains(name))) {
                matches.add("mod:" + mod.id());
            }
        }
        return matches;
    }

    private List<String> normalize(List<String> values) {
        return values.stream().map(this::normalize).toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
