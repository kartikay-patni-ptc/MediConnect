package com.example.demo.controllers;

import com.example.demo.model.Doctor;
import com.example.demo.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorsController {

	@Autowired
	private DoctorService doctorService;

	@GetMapping
	public ResponseEntity<List<Doctor>> query(@RequestParam(value = "q", required = false) String q) {
		return ResponseEntity.ok(doctorService.searchDoctorsBySpecialization(q));
	}
}



