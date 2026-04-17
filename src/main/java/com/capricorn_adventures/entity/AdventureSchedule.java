package com.capricorn_adventures.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name= "adventure_schedules")
public class AdventureSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adventure_id", nullable = false)
    private Adventure adventure;
   
   
    @Column(nullable = false)
    private LocalDateTime startDate;
    @Column(nullable = false)
    private LocalDateTime endDate;
    @Column(nullable = false)
    private Integer availableSlots;
    @Column
    private Integer totalCapacity;
    @Column(length = 255)
    private String assignedGuideName;
    @Column
    private Integer checkedInCustomerCount;
    @Column(nullable = false)
    private String status;

    // Getters and setters
    public Long getId() {
        return id;
    }   
    public void setId(Long id) {
        this.id = id;
    }
    public Adventure getAdventure() {
        return adventure;
    }
    public void setAdventure(Adventure adventure) {
        this.adventure = adventure;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    public LocalDateTime getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    public Integer getAvailableSlots() {
        return availableSlots;
    }
    public void setAvailableSlots(Integer availableSlots) {
        this.availableSlots = availableSlots;
    }
    public Integer getTotalCapacity() {
        return totalCapacity;
    }
    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }
    public String getAssignedGuideName() {
        return assignedGuideName;
    }
    public void setAssignedGuideName(String assignedGuideName) {
        this.assignedGuideName = assignedGuideName;
    }
    public Integer getCheckedInCustomerCount() {
        return checkedInCustomerCount;
    }
    public void setCheckedInCustomerCount(Integer checkedInCustomerCount) {
        this.checkedInCustomerCount = checkedInCustomerCount;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    
}