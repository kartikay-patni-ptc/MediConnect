package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentDto {
    private Long id;
    private String paymentMethod; // String representation of PaymentMethod enum
    private String transactionId;
    private BigDecimal amount;
    private String status; // String representation of PaymentStatus enum
    private LocalDateTime paymentDate;
}
