package com.example.demo.repository;

import com.example.demo.model.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Long> {
    List<DoctorSlot> findByDoctorId(Long doctorId);
}

