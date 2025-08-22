package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTrackingDto {
    private Long id;
    private String status; // String representation of DeliveryStatus enum
    private String currentLocation;
    private String estimatedDeliveryTime; // String representation of LocalDateTime
    private LocalDateTime lastUpdated;
    private String deliveryPartnerName;
    private String deliveryPartnerPhone;
}
