package com.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "rewards")
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waste_report_id")
    private WasteReport wasteReport;

    @Column(nullable = false)
    private Integer points;

    @Column(length = 50)
    private String type; // EARNED, REDEEMED

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ===== Constructors =====
    public Reward() {}

    public Reward(Long id, User user, WasteReport wasteReport, Integer points,
                  String type, String description, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.wasteReport = wasteReport;
        this.points = points;
        this.type = type;
        this.description = description;
        this.createdAt = createdAt;
    }

    // ===== Getters =====
    public Long getId() { return id; }
    public User getUser() { return user; }
    public WasteReport getWasteReport() { return wasteReport; }
    public Integer getPoints() { return points; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ===== Setters =====
    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setWasteReport(WasteReport wasteReport) { this.wasteReport = wasteReport; }
    public void setPoints(Integer points) { this.points = points; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Reward{id=" + id + ", points=" + points + ", type='" + type + "'}";
    }
}