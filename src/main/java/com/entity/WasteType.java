package com.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "waste_types")
public class WasteType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String category; // ORGANIC, RECYCLABLE, HAZARDOUS, GENERAL

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "base_points")
    private Integer basePoints = 10;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // ===== Constructors =====
    public WasteType() {}

    public WasteType(Long id, String name, String category, String description,
                     String iconUrl, Integer basePoints, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.iconUrl = iconUrl;
        this.basePoints = basePoints;
        this.isActive = isActive;
    }

    // ===== Getters =====
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public Integer getBasePoints() { return basePoints; }
    public Boolean getIsActive() { return isActive; }

    // ===== Setters =====
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public void setBasePoints(Integer basePoints) { this.basePoints = basePoints; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    @Override
    public String toString() {
        return "WasteType{id=" + id + ", name='" + name + "', category='" + category + "'}";
    }
}