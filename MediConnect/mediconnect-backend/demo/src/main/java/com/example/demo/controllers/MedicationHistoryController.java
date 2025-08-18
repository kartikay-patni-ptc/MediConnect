package com.example.demo.controllers;

import com.example.demo.model.MedicationHistory;
import com.example.demo.model.MedicationRequest;
import com.example.demo.service.MedicationHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient/history/medications")
@CrossOrigin(origins = "*")
public class MedicationHistoryController {

    @Autowired
    private MedicationHistoryService service;

    @GetMapping("/{patientId}")
    public ResponseEntity<List<MedicationHistory>> list(@PathVariable Long patientId) {
        return ResponseEntity.ok(service.listByPatient(patientId));
    }

    @PostMapping("/{patientId}")
    public ResponseEntity<?> create(@PathVariable Long patientId, @RequestBody MedicationRequest req) {
        try {
            MedicationHistory saved = service.add(patientId, req);
            return ResponseEntity.ok(Map.of("message", "Medication added", "medication", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MedicationRequest req) {
        try {
            MedicationHistory updated = service.update(id, req);
            return ResponseEntity.ok(Map.of("message", "Medication updated", "medication", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(Map.of("message", "Medication deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}


