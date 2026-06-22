package com.clientwatch.detection;

import com.clientwatch.model.ModInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Adds conservative warnings for malformed or suspicious values that are already visible to the server.
 */
public final class DetectionAnalyzer {
    private static final Pattern VERSION_PATTERN = Pattern.compile("[0-9]+(\\.[0-9A-Za-z_-]+)*");
    private static final Set<String> KNOWN_BRANDS = Set.of("vanilla", "fabric", "forge", "neoforge", "quilt", "paper", "spigot");
    private static final Set<String> SUSPICIOUS_WORDS = Set.of(
        "killaura", "triggerbot", "reach", "xray", "freecam", "esp", "baritone", "scaffold", "nuker", "fly", "blink"
    );

    public List<String> analyze(String brand, List<ModInfo> mods) {
        Set<String> warnings = new HashSet<>();
        String normalizedBrand = normalize(brand);
        if (normalizedBrand.isBlank() || normalizedBrand.equals("unknown")) {
            warnings.add("Client brand is unavailable.");
        } else if (!KNOWN_BRANDS.contains(normalizedBrand) && containsControlCharacters(brand)) {
            warnings.add("Client brand contains invalid control characters.");
        } else if (!KNOWN_BRANDS.contains(normalizedBrand)) {
            warnings.add("Client brand is not recognized as a common vanilla or loader brand.");
        }
        Set<String> modIds = new HashSet<>();
        for (ModInfo mod : mods) {
            String id = normalize(mod.id());
            if (!modIds.add(id)) {
                warnings.add("Duplicate mod id detected: " + mod.id());
            }
            if (!mod.version().isBlank() && !VERSION_PATTERN.matcher(mod.version()).matches()) {
                warnings.add("Mod has an unusual version value: " + mod.id());
            }
            String combined = normalize(mod.id() + " " + mod.name());
            if (SUSPICIOUS_WORDS.stream().anyMatch(combined::contains)) {
                warnings.add("Suspicious mod name detected: " + mod.id());
            }
        }
        if (mods.isEmpty()) {
            warnings.add("No reliable client-provided mod list is available.");
        }
        return List.copyOf(warnings);
    }

    private boolean containsControlCharacters(String value) {
        return value != null && value.chars().anyMatch(character -> Character.isISOControl(character) && !Character.isWhitespace(character));
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
