package com.clientwatch.model;

public record ModInfo(String id, String name, String version) {
    public boolean matches(String value) {
        String normalized = value.toLowerCase();
        return id.toLowerCase().contains(normalized)
            || name.toLowerCase().contains(normalized)
            || version.toLowerCase().contains(normalized);
    }
}
