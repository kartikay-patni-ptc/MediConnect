package com.example.demo.service;

import com.example.demo.model.Appointment;
import com.example.demo.model.DoctorSlot;
import com.example.demo.model.Patient;
import com.example.demo.model.Doctor;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.DoctorSlotRepository;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private DoctorSlotRepository doctorSlotRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private DoctorRepository doctorRepository;

    public Appointment createAppointment(Long patientId, Long doctorId, Long slotId, String notes, String aiSummary, 
                                       String doctorSummary, String patientAdvice, String prescribedMedicines, 
                                       String riskLevel, String redFlags, String homeRemedies, String specializationHint) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        Optional<DoctorSlot> slotOpt = doctorSlotRepository.findById(slotId);
        if (patientOpt.isEmpty() || doctorOpt.isEmpty() || slotOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid patient, doctor, or slot");
        }
        DoctorSlot slot = slotOpt.get();
        if (!slot.isAvailable()) {
            throw new IllegalStateException("Slot is not available");
        }
        slot.setAvailable(false);
        doctorSlotRepository.save(slot);
        Appointment appointment = new Appointment();
        appointment.setPatient(patientOpt.get());
        appointment.setDoctor(doctorOpt.get());
        appointment.setSlot(slot);
        appointment.setStatus(Appointment.Status.Pending);
        appointment.setNotes(notes);
        appointment.setAiSummary(aiSummary);
        appointment.setDoctorSummary(doctorSummary);
        appointment.setPatientAdvice(patientAdvice);
        appointment.setPrescribedMedicines(prescribedMedicines);
        appointment.setRiskLevel(riskLevel);
        appointment.setRedFlags(redFlags);
        appointment.setHomeRemedies(homeRemedies);
        appointment.setSpecializationHint(specializationHint);
        appointment.setCreatedAt(LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public Appointment updateStatus(Long appointmentId, Appointment.Status status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }
}

