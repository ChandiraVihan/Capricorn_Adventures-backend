package com.capricorn_adventures.dto;

import com.capricorn_adventures.entity.RoomServiceOrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class RoomServiceOrderCardDTO {

    private Long orderId;
    private Integer roomNumber;
    private Integer floorNumber;
    private List<String> itemsOrdered;
    private LocalDateTime placedAt;
    private UUID assignedStaffId;
    private String assignedStaffName;
    private RoomServiceOrderStatus status;
    private boolean staleFlag;
    private LocalDateTime lastStatusUpdatedAt;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public List<String> getItemsOrdered() {
        return itemsOrdered;
    }

    public void setItemsOrdered(List<String> itemsOrdered) {
        this.itemsOrdered = itemsOrdered;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(LocalDateTime placedAt) {
        this.placedAt = placedAt;
    }

    public UUID getAssignedStaffId() {
        return assignedStaffId;
    }

    public void setAssignedStaffId(UUID assignedStaffId) {
        this.assignedStaffId = assignedStaffId;
    }

    public String getAssignedStaffName() {
        return assignedStaffName;
    }

    public void setAssignedStaffName(String assignedStaffName) {
        this.assignedStaffName = assignedStaffName;
    }

    public RoomServiceOrderStatus getStatus() {
        return status;
    }

    public void setStatus(RoomServiceOrderStatus status) {
        this.status = status;
    }

    public boolean isStaleFlag() {
        return staleFlag;
    }

    public void setStaleFlag(boolean staleFlag) {
        this.staleFlag = staleFlag;
    }

    public LocalDateTime getLastStatusUpdatedAt() {
        return lastStatusUpdatedAt;
    }

    public void setLastStatusUpdatedAt(LocalDateTime lastStatusUpdatedAt) {
        this.lastStatusUpdatedAt = lastStatusUpdatedAt;
    }
}
