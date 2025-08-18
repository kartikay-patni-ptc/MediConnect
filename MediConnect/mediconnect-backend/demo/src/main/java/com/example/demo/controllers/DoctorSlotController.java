package com.example.demo.controllers;

import com.example.demo.model.DoctorSlot;
import com.example.demo.service.DoctorSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
public class DoctorSlotController {
    @Autowired
    private DoctorSlotService doctorSlotService;

    @PostMapping("/{doctorId}/slots")
    public DoctorSlot createSlot(@PathVariable Long doctorId, @RequestBody Map<String, String> payload) {
        LocalDateTime startTime = LocalDateTime.parse(payload.get("startTime"));
        LocalDateTime endTime = LocalDateTime.parse(payload.get("endTime"));
        return doctorSlotService.createSlot(doctorId, startTime, endTime);
    }

    @GetMapping("/{doctorId}/slots")
    public List<DoctorSlot> getSlotsByDoctor(@PathVariable Long doctorId) {
        return doctorSlotService.getSlotsByDoctor(doctorId);
    }

    @PutMapping("/slots/{slotId}/availability")
    public DoctorSlot updateAvailability(@PathVariable Long slotId, @RequestBody Map<String, Boolean> payload) {
        boolean available = payload.get("available");
        return doctorSlotService.updateAvailability(slotId, available);
    }
}

