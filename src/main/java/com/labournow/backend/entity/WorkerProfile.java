package com.labournow.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "worker_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String aadhaarNumber;

    private String panNumber;

    @Column(nullable = false)
    private boolean isVerified = false;

    @Column(nullable = false)
    private boolean isAvailable = true;

    private Double currentLatitude;
    private Double currentLongitude;
    
    // Service radius in kilometers
    private Integer serviceRadiusKm = 10;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    private Integer totalReviews = 0;
    private Integer totalJobsCompleted = 0;

    // A simple representation of primary skill for MVP
    @Column(nullable = false)
    private String primarySkill;

    @Column(precision = 8, scale = 2)
    private BigDecimal hourlyRate = new BigDecimal("300.00");

    private boolean isOnline = true;

    private BigDecimal totalEarnings = BigDecimal.ZERO;
}
