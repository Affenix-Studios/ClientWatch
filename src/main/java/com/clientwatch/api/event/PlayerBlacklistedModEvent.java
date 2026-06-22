package com.clientwatch.api.event;

import com.clientwatch.model.ClientDetection;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerBlacklistedModEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final ClientDetection detection;

    public PlayerBlacklistedModEvent(ClientDetection detection, boolean async) {
        super(async);
        this.detection = detection;
    }

    public ClientDetection detection() {
        return detection;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
