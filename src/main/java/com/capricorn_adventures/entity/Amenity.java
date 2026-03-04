package com.capricorn_adventures.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "amenities")
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Useful for the frontend to know which icon to display
    private String iconIdentifier; 

    // No-argument constructor (required by JPA)
    public Amenity() {
    }

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

    public String getIconIdentifier() {
        return iconIdentifier;
    }

    public void setIconIdentifier(String iconIdentifier) {
        this.iconIdentifier = iconIdentifier;
    }
}