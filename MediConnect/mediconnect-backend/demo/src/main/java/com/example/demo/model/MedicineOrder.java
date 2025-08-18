package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "medicine_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MedicineOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber; // Unique order identifier

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = true) // null until assigned
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PharmacyStore pharmacy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType = OrderType.DELIVERY;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(nullable = true)
    private String deliveryPincode;

    @Column(nullable = true)
    private String patientPhoneNumber;

    @Column(nullable = true)
    private String specialInstructions; // Patient's special delivery instructions

    @Column(nullable = true)
    private String pharmacyNotes; // Pharmacy's notes about the order

    @Column(nullable = true)
    private String rejectionReason; // Reason if rejected by pharmacy

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DeliveryTracking deliveryTracking;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OrderPayment payment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private LocalDateTime acceptedAt;

    @Column(nullable = true)
    private LocalDateTime expectedDeliveryTime;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (orderNumber == null) {
            orderNumber = generateOrderNumber();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }

    public enum OrderStatus {
        PENDING,           // Order created, waiting for pharmacy assignment
        PHARMACY_ASSIGNED, // Pharmacy assigned, waiting for acceptance
        ACCEPTED,          // Pharmacy accepted the order
        REJECTED,          // Pharmacy rejected the order
        PREPARING,         // Pharmacy is preparing the medicines
        READY_FOR_PICKUP,  // Medicines ready, waiting for delivery partner
        OUT_FOR_DELIVERY,  // Delivery partner picked up, on the way
        DELIVERED,         // Successfully delivered to patient
        CANCELLED,         // Order cancelled by patient or system
        REFUNDED           // Order refunded
    }

    public enum OrderType {
        DELIVERY,          // Home delivery
        PICKUP             // Patient will pick up from pharmacy
    }
}
