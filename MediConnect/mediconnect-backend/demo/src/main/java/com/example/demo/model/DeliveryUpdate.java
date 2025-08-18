package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_updates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DeliveryUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_tracking_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DeliveryTracking deliveryTracking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UpdateType updateType;

    @Column(nullable = false)
    private String title; // Update title

    @Column(columnDefinition = "TEXT")
    private String description; // Detailed description

    @Column(nullable = true)
    private Double latitude; // Location when update was made

    @Column(nullable = true)
    private Double longitude; // Location when update was made

    @Column(nullable = true)
    private String location; // Human readable location

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = true)
    private String updatedBy; // Who made the update (delivery partner, system, etc.)

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public enum UpdateType {
        STATUS_CHANGE,     // Status changed
        LOCATION_UPDATE,   // Location updated
        DELAY_NOTIFICATION, // Delivery delayed
        SPECIAL_NOTE,      // Special information
        DELIVERY_ATTEMPT,  // Delivery attempt made
        CUSTOMER_CONTACT   // Customer contacted
    }
}
