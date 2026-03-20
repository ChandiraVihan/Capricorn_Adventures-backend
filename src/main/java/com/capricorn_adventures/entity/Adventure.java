package com.capricorn_adventures.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "adventures")
public class Adventure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "adventure", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdventureSchedule> schedules;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private AdventureCategory category;
    
    @Column(nullable = false)
    private String name;

    @Column(length = 1000) 
    private String description;

    @Column(nullable = false)
    private BigDecimal basePrice;


    @Column(nullable = true)
    private String primaryImageUrl;

    @Column(nullable = true)
    private String location;

    @Column(nullable = true)
    private String difficultyLevel;

    @Column(nullable = true)
    private Integer minAge;

    @Column(nullable = true)
    private String imageUrls;

    // Getters and setters
    public Long getId() {   
        return id;
    }   
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;

    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public AdventureCategory getCategory() {
        return category;
    }

    public void setCategory(AdventureCategory category) {
        this.category = category;
    }

    public List<AdventureSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<AdventureSchedule> schedules) {
        this.schedules = schedules;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }
    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getDifficultyLevel() {
        return difficultyLevel;
    }
    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    public Integer getMinAge() {
        return minAge;
    }
    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }
    public String getImageUrls() {
        return imageUrls;
    }
    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }



}

