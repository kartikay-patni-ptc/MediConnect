package com.example.demo.model;

import lombok.Data;

@Data
public class DoctorVerificationRequest {
    private String fullName;
    private String registrationNumber;
    private Long userId;
}