package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MedicineOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_medicine_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PrescriptionMedicine prescriptionMedicine;

    @Column(nullable = false)
    private String medicineName;

    @Column(nullable = false)
    private String dosage;

    @Column(nullable = false)
    private Integer quantityRequested; // Quantity from prescription

    @Column(nullable = true)
    private Integer quantityAvailable; // What pharmacy has in stock

    @Column(nullable = true)
    private Integer quantityProvided; // What pharmacy will provide

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal unitPrice; // Price per unit set by pharmacy

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal totalPrice; // quantity * unit price

    @Column(nullable = true)
    private String brandName; // Specific brand pharmacy will provide

    @Column(nullable = true)
    private String manufacturerName; // Medicine manufacturer

    @Column(nullable = true)
    private String batchNumber; // Batch number for tracking

    @Column(nullable = true)
    private String expiryDate; // Medicine expiry date

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.PENDING;

    @Column(nullable = true)
    private String substitutionNote; // If generic substitution is made

    @Column(nullable = true)
    private String unavailabilityReason; // Why item is not available

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ItemStatus {
        PENDING,           // Item added to order
        AVAILABLE,         // Pharmacy has the item
        PARTIALLY_AVAILABLE, // Pharmacy has partial quantity
        SUBSTITUTED,       // Generic/alternative provided
        UNAVAILABLE        // Item not available
    }
}
