package com.capricorn_adventures.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cancellation_policies")
public class CancellationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String category; // e.g., "ADVENTURE", "ROOM", or specific adventure type

    @Column(nullable = false)
    private int fullRefundLimitHours;

    @Column(nullable = false)
    private int partialRefundLimitHours;

    @Column(nullable = false)
    private BigDecimal partialRefundPercentage;

    public CancellationPolicy() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getFullRefundLimitHours() { return fullRefundLimitHours; }
    public void setFullRefundLimitHours(int fullRefundLimitHours) { this.fullRefundLimitHours = fullRefundLimitHours; }

    public int getPartialRefundLimitHours() { return partialRefundLimitHours; }
    public void setPartialRefundLimitHours(int partialRefundLimitHours) { this.partialRefundLimitHours = partialRefundLimitHours; }

    public BigDecimal getPartialRefundPercentage() { return partialRefundPercentage; }
    public void setPartialRefundPercentage(BigDecimal partialRefundPercentage) { this.partialRefundPercentage = partialRefundPercentage; }
}
