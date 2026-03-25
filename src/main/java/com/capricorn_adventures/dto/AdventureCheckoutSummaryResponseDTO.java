package com.capricorn_adventures.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class AdventureCheckoutSummaryResponseDTO {
    private Long checkoutId;
    private Long adventureId;
    private String adventureName;
    private Long scheduleId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer participants;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String status;
    private boolean canProceedAsGuest;
    private boolean canLoginOrRegister;
    private boolean selectionRetained;
    private String authMessage;

    public Long getCheckoutId() {
        return checkoutId;
    }

    public void setCheckoutId(Long checkoutId) {
        this.checkoutId = checkoutId;
    }

    public Long getAdventureId() {
        return adventureId;
    }

    public void setAdventureId(Long adventureId) {
        this.adventureId = adventureId;
    }

    public String getAdventureName() {
        return adventureName;
    }

    public void setAdventureName(String adventureName) {
        this.adventureName = adventureName;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getParticipants() {
        return participants;
    }

    public void setParticipants(Integer participants) {
        this.participants = participants;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCanProceedAsGuest() {
        return canProceedAsGuest;
    }

    public void setCanProceedAsGuest(boolean canProceedAsGuest) {
        this.canProceedAsGuest = canProceedAsGuest;
    }

    public boolean isCanLoginOrRegister() {
        return canLoginOrRegister;
    }

    public void setCanLoginOrRegister(boolean canLoginOrRegister) {
        this.canLoginOrRegister = canLoginOrRegister;
    }

    public boolean isSelectionRetained() {
        return selectionRetained;
    }

    public void setSelectionRetained(boolean selectionRetained) {
        this.selectionRetained = selectionRetained;
    }

    public String getAuthMessage() {
        return authMessage;
    }

    public void setAuthMessage(String authMessage) {
        this.authMessage = authMessage;
    }
}
