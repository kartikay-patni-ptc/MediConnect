package com.example.demo.controllers;


import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.service.PharmacyStoreService;
import com.example.demo.model.PharmacyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pharmacist")
@CrossOrigin(origins = "*")
public class PharmacistsController {

    @Autowired
    private UserService userService;

    @Autowired
    private PharmacyStoreService pharmacyStoreService;

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<?> getDashboard(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            Optional<PharmacyStore> store = pharmacyStoreService.getStoreByUser(user);
            if (store.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "store", store.get(),
                        "orders", List.of(), // Empty for now
                        "totalOrders", 0,
                        "pendingOrders", 0
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            Optional<PharmacyStore> store = pharmacyStoreService.getStoreByUser(user);
            if (store.isPresent()) {
                return ResponseEntity.ok(store.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}