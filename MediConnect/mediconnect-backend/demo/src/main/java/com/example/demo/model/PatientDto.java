package com.example.demo.model;

import lombok.Data;

@Data
public class PatientDto {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String emergencyContact;
    private String medicalHistory;
    private Long userId;
}