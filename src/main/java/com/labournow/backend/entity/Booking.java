package com.labournow.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private User worker; // Nullable initially until assigned

    @Column(nullable = false)
    private String category; // e.g., Electrician, Plumber

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    // Location of the job
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String addressDetails;

    private String problemDescription;

    // ETA provided by worker when accepting the job
    private String eta;

    // Initial estimation or agreed price
    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedPrice;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING,       // Customer requested, waiting for worker
        ACCEPTED,      // Worker accepted
        ON_THE_WAY,    // Worker is traveling to location
        STARTED,       // Worker has started the job
        COMPLETED,     // Job finished
        CANCELLED      // Job cancelled
    }
}
