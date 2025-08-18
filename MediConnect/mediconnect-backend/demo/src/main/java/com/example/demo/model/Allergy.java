package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "allergy")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Allergy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@Column(nullable = false)
	private String allergen;

	@Column(nullable = true)
	private String reaction;
}



