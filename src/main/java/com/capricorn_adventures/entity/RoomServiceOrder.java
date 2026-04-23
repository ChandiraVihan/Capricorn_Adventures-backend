package com.capricorn_adventures.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "room_service_orders")
public class RoomServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false)
    private Integer roomNumber;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @ElementCollection
    @CollectionTable(name = "room_service_order_items", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "item_name", nullable = false, length = 255)
    private List<String> itemsOrdered = new ArrayList<>();

    @Column(name = "placed_at", nullable = false)
    private LocalDateTime placedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomServiceOrderStatus status;

    @Column(name = "last_status_updated_at", nullable = false)
    private LocalDateTime lastStatusUpdatedAt;

    @Column(name = "stale_flag", nullable = false)
    private boolean staleFlag;

    @Column(name = "stale_alerted_at")
    private LocalDateTime staleAlertedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public User getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(User assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    public RoomServiceOrderStatus getStatus() {
        return status;
    }

    public void setStatus(RoomServiceOrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastStatusUpdatedAt() {
        return lastStatusUpdatedAt;
    }

    public void setLastStatusUpdatedAt(LocalDateTime lastStatusUpdatedAt) {
        this.lastStatusUpdatedAt = lastStatusUpdatedAt;
    }

    public boolean isStaleFlag() {
        return staleFlag;
    }

    public void setStaleFlag(boolean staleFlag) {
        this.staleFlag = staleFlag;
    }

    public LocalDateTime getStaleAlertedAt() {
        return staleAlertedAt;
    }

    public void setStaleAlertedAt(LocalDateTime staleAlertedAt) {
        this.staleAlertedAt = staleAlertedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
