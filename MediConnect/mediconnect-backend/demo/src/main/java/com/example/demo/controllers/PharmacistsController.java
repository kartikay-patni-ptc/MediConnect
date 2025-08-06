package com.example.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pharmacist")
public class PharmacistsController {

    @GetMapping("/dashboard")
    public String pharmacistDashboard() {
        return "Pharmacist Dashboard";
    }
}