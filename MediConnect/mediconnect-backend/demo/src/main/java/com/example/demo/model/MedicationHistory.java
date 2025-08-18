package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "medication_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@Column(nullable = false)
	private String medicationName;

	@Column(nullable = false)
	private String dosage;

	@Column(nullable = true)
	private String frequency;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = true)
	private LocalDate endDate;

	@Column(columnDefinition = "TEXT")
	private String notes;
}



