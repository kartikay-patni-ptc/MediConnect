package com.example.demo.model;

import com.example.demo.model.MedicineOrder.OrderStatus;
import com.example.demo.model.MedicineOrder.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineOrderDto {
    private Long id;
    private String orderNumber;
    private PrescriptionDto prescription;
    private PatientDto patient;
    private PharmacyStoreDto pharmacy;
    private OrderStatus status;
    private OrderType orderType;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private BigDecimal finalAmount;
    private String deliveryAddress;
    private String deliveryPincode;
    private String patientPhoneNumber;
    private String specialInstructions;
    private String pharmacyNotes;
    private String rejectionReason;
    private List<OrderItemDto> orderItems;
    private DeliveryTrackingDto deliveryTracking;
    private OrderPaymentDto payment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime expectedDeliveryTime;
}

