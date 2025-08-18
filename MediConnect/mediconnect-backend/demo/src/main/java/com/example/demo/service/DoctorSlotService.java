package com.example.demo.service;

import com.example.demo.model.DoctorSlot;
import com.example.demo.model.Doctor;
import com.example.demo.repository.DoctorSlotRepository;
import com.example.demo.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorSlotService {
    @Autowired
    private DoctorSlotRepository doctorSlotRepository;
    @Autowired
    private DoctorRepository doctorRepository;

    public DoctorSlot createSlot(Long doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid doctor");
        }
        DoctorSlot slot = new DoctorSlot();
        slot.setDoctor(doctorOpt.get());
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setAvailable(true);
        return doctorSlotRepository.save(slot);
    }

    public List<DoctorSlot> getSlotsByDoctor(Long doctorId) {
        return doctorSlotRepository.findByDoctorId(doctorId);
    }

    public DoctorSlot updateAvailability(Long slotId, boolean available) {
        DoctorSlot slot = doctorSlotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        slot.setAvailable(available);
        return doctorSlotRepository.save(slot);
    }
}

