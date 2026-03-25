package com.capricorn_adventures.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name= "adventure_schedules")
public class AdventureSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adventure_id", nullable = false)
    private Adventure adventure;
   
   
    @Column(nullable = false)
    private LocalDateTime startDate;
    @Column(nullable = false)
    private LocalDateTime endDate;
    @Column(nullable = false)
    private Integer availableSlots;
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
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    
}