package com.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "waste_reports")
public class WasteReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "waste_type_id", nullable = false)
    private WasteType wasteType;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "quantity_kg")
    private Double quantityKg;

    @Column(length = 50)
    private String status = "PENDING";

    @Column(name = "reward_points_earned")
    private Integer rewardPointsEarned = 0;
    
    @Column(name = "citizen_note", columnDefinition = "TEXT")
    private String citizenNote;

    // Getter & Setter
    public String getCitizenNote() { return citizenNote; }
    public void setCitizenNote(String citizenNote) { this.citizenNote = citizenNote; }

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public WasteReport() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCitizen() { return citizen; }
    public void setCitizen(User citizen) { this.citizen = citizen; }

    public WasteType getWasteType() { return wasteType; }
    public void setWasteType(WasteType wasteType) { this.wasteType = wasteType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getQuantityKg() { return quantityKg; }
    public void setQuantityKg(Double quantityKg) { this.quantityKg = quantityKg; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRewardPointsEarned() { return rewardPointsEarned; }
    public void setRewardPointsEarned(Integer rewardPointsEarned) { this.rewardPointsEarned = rewardPointsEarned; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}