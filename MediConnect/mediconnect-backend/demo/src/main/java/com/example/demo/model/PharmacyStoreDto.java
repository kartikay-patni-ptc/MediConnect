package com.example.demo.model;

import lombok.Data;

@Data
public class PharmacyStoreDto {
    private String name;
    private String ownerName;
    private String licenseNumber;
    private String phoneNumber;
    private String email;
    private String address;
    private String description;
    private Long userId;
}
