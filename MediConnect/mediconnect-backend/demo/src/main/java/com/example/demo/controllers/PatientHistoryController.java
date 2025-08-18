package com.example.demo.controllers;

import com.example.demo.service.PatientHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient/history")
@CrossOrigin(origins = "*")
public class PatientHistoryController {

	@Autowired
	private PatientHistoryService patientHistoryService;

	@GetMapping("/{patientId}")
	public ResponseEntity<?> getHistory(@PathVariable Long patientId) {
		try {
			return ResponseEntity.ok(patientHistoryService.getPatientHistory(patientId));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		}
	}
}



