package com.example.demo.repository;

import com.example.demo.model.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long> {
	List<Allergy> findByPatientId(Long patientId);
}



