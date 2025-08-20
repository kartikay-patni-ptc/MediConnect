package com.example.demo.controllers;

import com.example.demo.model.DoctorSlot;
import com.example.demo.service.DoctorSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorSlotController {
    @Autowired
    private DoctorSlotService doctorSlotService;

    @PostMapping("/{doctorId}/slots")
    public ResponseEntity<?> createSlot(@PathVariable Long doctorId, @RequestBody Map<String, String> payload) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(payload.get("startTime"));
            LocalDateTime endTime = LocalDateTime.parse(payload.get("endTime"));

            DoctorSlot slot = doctorSlotService.createSlot(doctorId, startTime, endTime);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Slot created successfully");
            response.put("slot", slot);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create slot: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{doctorId}/slots")
    public ResponseEntity<?> getSlotsByDoctor(@PathVariable Long doctorId) {
        try {
            List<DoctorSlot> slots = doctorSlotService.getSlotsByDoctor(doctorId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("slots", slots);
            response.put("count", slots.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get slots: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/slots/{slotId}/availability")
    public ResponseEntity<?> updateAvailability(@PathVariable Long slotId, @RequestBody Map<String, Boolean> payload) {
        try {
            boolean available = payload.get("available");
            DoctorSlot slot = doctorSlotService.updateAvailability(slotId, available);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Slot availability updated successfully");
            response.put("slot", slot);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update slot availability: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<?> deleteSlot(@PathVariable Long slotId) {
        try {
            doctorSlotService.deleteSlot(slotId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Slot deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete slot: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}