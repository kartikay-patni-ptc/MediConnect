package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrderPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MedicineOrder order;

    @Column(nullable = false)
    private String paymentId; // External payment gateway ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(nullable = true)
    private String transactionId; // Bank/gateway transaction ID

    @Column(nullable = true)
    private String gatewayResponse; // Payment gateway response

    @Column(nullable = true)
    private String failureReason; // If payment failed

    @Column(nullable = true)
    private String refundId; // Refund transaction ID

    @Column(nullable = true)
    private String refundReason; // Reason for refund

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime paidAt;

    @Column(nullable = true)
    private LocalDateTime refundedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (paymentId == null) {
            paymentId = generatePaymentId();
        }
    }

    private String generatePaymentId() {
        return "PAY" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }

    public enum PaymentMethod {
        CREDIT_CARD,
        DEBIT_CARD,
        UPI,
        NET_BANKING,
        DIGITAL_WALLET,
        CASH_ON_DELIVERY
    }

    public enum PaymentStatus {
        PENDING,       // Payment initiated
        PROCESSING,    // Payment being processed
        COMPLETED,     // Payment successful
        FAILED,        // Payment failed
        CANCELLED,     // Payment cancelled
        REFUNDED,      // Payment refunded
        PARTIALLY_REFUNDED // Partial refund
    }
}
