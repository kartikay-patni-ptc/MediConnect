package com.example.demo.model;


import lombok.Data;

@Data
public class DoctorDto {
    private String firstName;
    private String lastName;
    private String specialization;
    private String licenseNumber;
    private String phoneNumber;
    private String email;
    private Integer experience;
    private String education;
    private String hospital;
    private String address;
    private String description;
    private Long userId;
    private Boolean wasVerified;
}