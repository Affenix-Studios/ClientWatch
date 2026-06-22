package com.clientwatch.detection;

import com.clientwatch.model.ClientLoader;
import com.clientwatch.model.ModInfo;

import java.util.List;
import java.util.Locale;

public final class LoaderResolver {
    public ClientLoader resolve(String brand, List<ModInfo> mods) {
        String normalized = brand == null ? "" : brand.toLowerCase(Locale.ROOT);
        if (normalized.contains("fabric")) return ClientLoader.FABRIC;
        if (normalized.contains("neoforge")) return ClientLoader.NEOFORGE;
        if (normalized.contains("forge")) return ClientLoader.FORGE;
        if (normalized.contains("quilt")) return ClientLoader.QUILT;
        if (normalized.contains("liteloader")) return ClientLoader.LITELOADER;
        if (normalized.contains("rift")) return ClientLoader.RIFT;
        if (mods.stream().anyMatch(mod -> mod.id().equalsIgnoreCase("fabricloader"))) return ClientLoader.FABRIC;
        return normalized.isBlank() || normalized.equals("vanilla") ? ClientLoader.VANILLA : ClientLoader.UNKNOWN;
    }
}
