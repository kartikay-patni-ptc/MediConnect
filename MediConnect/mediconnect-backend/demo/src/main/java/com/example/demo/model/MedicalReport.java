package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "medical_report")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@Column(nullable = false)
	private String type;

	@Column(nullable = false)
	private LocalDate date;

	@Column(columnDefinition = "TEXT")
	private String summary;

	@Column(nullable = true)
	private String fileUrl;
}



