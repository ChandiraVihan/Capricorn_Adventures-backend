package com.capricorn_adventures.dto;

import java.time.LocalDateTime;
import java.util.List;

public class RoomServiceDashboardResponseDTO {

    private LocalDateTime generatedAt;
    private boolean autoRefreshEnabled;
    private int staleThresholdMinutes;
    private List<RoomServiceOrderCardDTO> activeOrders;

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public boolean isAutoRefreshEnabled() {
        return autoRefreshEnabled;
    }

    public void setAutoRefreshEnabled(boolean autoRefreshEnabled) {
        this.autoRefreshEnabled = autoRefreshEnabled;
    }

    public int getStaleThresholdMinutes() {
        return staleThresholdMinutes;
    }

    public void setStaleThresholdMinutes(int staleThresholdMinutes) {
        this.staleThresholdMinutes = staleThresholdMinutes;
    }

    public List<RoomServiceOrderCardDTO> getActiveOrders() {
        return activeOrders;
    }

    public void setActiveOrders(List<RoomServiceOrderCardDTO> activeOrders) {
        this.activeOrders = activeOrders;
    }
}
