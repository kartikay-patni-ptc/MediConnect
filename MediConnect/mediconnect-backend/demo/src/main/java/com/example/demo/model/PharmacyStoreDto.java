package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyStoreDto {
    private Long id;
    private String name;
    private String ownerName;
    private String licenseNumber;
    private String phoneNumber;
    private String email;
    private String address;
    private String description;
    private Double latitude;
    private Double longitude;
    // Note: User object is intentionally excluded to prevent password exposure
}
