package com.example.demo.controllers;


import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.model.PharmacyStoreDto;
import com.example.demo.model.PharmacyStore;
import com.example.demo.service.PharmacyStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/pharmacy")
@CrossOrigin(origins = "*")
public class PharmacyStoreController {

    @Autowired
    private PharmacyStoreService service;

    @Autowired
    private UserService userService;

    @PostMapping("/create-profile")
    public ResponseEntity<?> createPharmacyProfile(@RequestBody PharmacyStoreDto dto) {
        try {
            // Validate required fields
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Store name is required");
            }
            if (dto.getOwnerName() == null || dto.getOwnerName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Owner name is required");
            }
            if (dto.getLicenseNumber() == null || dto.getLicenseNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("License number is required");
            }
            if (dto.getPhoneNumber() == null || dto.getPhoneNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Phone number is required");
            }
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            if (dto.getAddress() == null || dto.getAddress().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Address is required");
            }
            if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Description is required");
            }
            if (dto.getUserId() == null) {
                return ResponseEntity.badRequest().body("User ID is required");
            }

            PharmacyStore store = service.createPharmacyStore(dto);
            return ResponseEntity.ok(store);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating pharmacy profile: " + e.getMessage());
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getPharmacyProfile(@PathVariable Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Optional<PharmacyStore> store = service.getStoreByUser(user);
        if (store.isPresent()) {
            return ResponseEntity.ok(store.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updatePharmacyProfile(@PathVariable Long id, @RequestBody PharmacyStoreDto dto) {
        try {
            PharmacyStore updatedStore = service.updateStore(id, dto);
            return ResponseEntity.ok(updatedStore);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating pharmacy profile: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<?> getDashboardData(@PathVariable Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Optional<PharmacyStore> store = service.getStoreByUser(user);
        if (store.isPresent()) {
            // For now, return basic dashboard data
            // In the future, this can include orders, analytics, etc.
            return ResponseEntity.ok(Map.of(
                    "store", store.get(),
                    "orders", List.of(), // Empty orders for now
                    "totalOrders", 0,
                    "pendingOrders", 0
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public PharmacyStore createStore(@RequestBody PharmacyStore store) {
        return service.createPharmacyStore(new PharmacyStoreDto());
    }

    @GetMapping
    public List<PharmacyStore> getStores() {
        return service.getAllStores();
    }
}