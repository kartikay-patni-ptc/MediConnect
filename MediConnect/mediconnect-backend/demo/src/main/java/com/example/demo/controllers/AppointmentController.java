package com.example.demo.controllers;

import com.example.demo.model.Appointment;
import com.example.demo.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public Appointment createAppointment(@RequestBody Map<String, Object> payload) {
        Long patientId = Long.valueOf(payload.get("patientId").toString());
        Long doctorId = Long.valueOf(payload.get("doctorId").toString());
        Long slotId = Long.valueOf(payload.get("slotId").toString());
        String notes = (String) payload.getOrDefault("notes", null);
        String aiSummary = (String) payload.getOrDefault("aiSummary", null);
        String doctorSummary = (String) payload.getOrDefault("doctorSummary", null);
        String patientAdvice = (String) payload.getOrDefault("patientAdvice", null);
        String prescribedMedicines = (String) payload.getOrDefault("prescribedMedicines", null);
        String riskLevel = (String) payload.getOrDefault("riskLevel", null);
        String redFlags = (String) payload.getOrDefault("redFlags", null);
        String homeRemedies = (String) payload.getOrDefault("homeRemedies", null);
        String specializationHint = (String) payload.getOrDefault("specializationHint", null);
        
        return appointmentService.createAppointment(
            patientId, doctorId, slotId, notes, aiSummary, 
            doctorSummary, patientAdvice, prescribedMedicines, 
            riskLevel, redFlags, homeRemedies, specializationHint
        );
    }

    @GetMapping("/patient/{id}")
    public List<Appointment> getAppointmentsByPatient(@PathVariable Long id) {
        return appointmentService.getAppointmentsByPatient(id);
    }

    @GetMapping("/doctor/{id}")
    public List<Appointment> getAppointmentsByDoctor(@PathVariable Long id) {
        return appointmentService.getAppointmentsByDoctor(id);
    }

    @GetMapping("/{id}")
    public Appointment getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }

    @PutMapping("/{id}/status")
    public Appointment updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Appointment.Status status = Appointment.Status.valueOf(payload.get("status"));
        return appointmentService.updateStatus(id, status);
    }
}

