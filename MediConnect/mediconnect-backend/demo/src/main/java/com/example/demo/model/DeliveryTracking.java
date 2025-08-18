package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "delivery_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DeliveryTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MedicineOrder order;

    @Column(nullable = false)
    private String trackingNumber; // Unique tracking identifier

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    @Column(nullable = true)
    private String deliveryPartnerName; // Name of delivery person

    @Column(nullable = true)
    private String deliveryPartnerPhone; // Contact number

    @Column(nullable = true)
    private String vehicleNumber; // Delivery vehicle number

    @Column(nullable = true)
    private Double currentLatitude; // Real-time location

    @Column(nullable = true)
    private Double currentLongitude; // Real-time location

    @Column(nullable = true)
    private Double estimatedDistance; // Distance to destination in KM

    @Column(nullable = true)
    private Integer estimatedTimeMinutes; // ETA in minutes

    @Column(nullable = true)
    private LocalDateTime pickupTime; // When picked up from pharmacy

    @Column(nullable = true)
    private LocalDateTime deliveryTime; // When delivered to patient

    @Column(nullable = true)
    private LocalDateTime estimatedDeliveryTime; // Expected delivery time

    @Column(columnDefinition = "TEXT")
    private String deliveryInstructions; // Special delivery instructions

    @Column(columnDefinition = "TEXT")
    private String deliveryNotes; // Delivery person's notes

    @Column(nullable = true)
    private String deliveryProofImageUrl; // Photo proof of delivery

    @Column(nullable = true)
    private String recipientName; // Who received the delivery

    @Column(nullable = true)
    private String recipientSignature; // Digital signature if applicable

    @OneToMany(mappedBy = "deliveryTracking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryUpdate> deliveryUpdates;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (trackingNumber == null) {
            trackingNumber = generateTrackingNumber();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateTrackingNumber() {
        return "TRK" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }

    public enum DeliveryStatus {
        PENDING,           // Waiting for pickup
        ASSIGNED,          // Delivery partner assigned
        PICKED_UP,         // Picked up from pharmacy
        IN_TRANSIT,        // On the way to patient
        NEARBY,            // Delivery partner is nearby
        DELIVERED,         // Successfully delivered
        FAILED_DELIVERY,   // Delivery attempt failed
        RETURNED_TO_PHARMACY, // Returned to pharmacy
        CANCELLED          // Delivery cancelled
    }
}
