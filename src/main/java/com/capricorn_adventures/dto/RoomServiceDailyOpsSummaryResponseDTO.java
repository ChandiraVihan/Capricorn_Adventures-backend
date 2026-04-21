package com.capricorn_adventures.dto;

import java.time.LocalDate;
import java.util.List;

public class RoomServiceDailyOpsSummaryResponseDTO {

    private LocalDate businessDate;
    private int totalOrders;
    private long averageDeliveryMinutes;
    private int unresolvedOrdersCount;
    private List<RoomServiceOrderCardDTO> unresolvedOrders;

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getAverageDeliveryMinutes() {
        return averageDeliveryMinutes;
    }

    public void setAverageDeliveryMinutes(long averageDeliveryMinutes) {
        this.averageDeliveryMinutes = averageDeliveryMinutes;
    }

    public int getUnresolvedOrdersCount() {
        return unresolvedOrdersCount;
    }

    public void setUnresolvedOrdersCount(int unresolvedOrdersCount) {
        this.unresolvedOrdersCount = unresolvedOrdersCount;
    }

    public List<RoomServiceOrderCardDTO> getUnresolvedOrders() {
        return unresolvedOrders;
    }

    public void setUnresolvedOrders(List<RoomServiceOrderCardDTO> unresolvedOrders) {
        this.unresolvedOrders = unresolvedOrders;
    }
}
