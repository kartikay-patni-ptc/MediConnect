package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private FileUploadService fileUploadService;

    public Prescription createPrescription(Long appointmentId, String diagnosis, String symptoms, 
                                         String doctorNotes, List<PrescriptionMedicine> medicines) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (!appointmentOpt.isPresent()) {
            throw new RuntimeException("Appointment not found with id: " + appointmentId);
        }

        Appointment appointment = appointmentOpt.get();
        
        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);
        prescription.setDoctor(appointment.getDoctor());
        prescription.setPatient(appointment.getPatient());
        prescription.setDiagnosis(diagnosis);
        prescription.setSymptoms(symptoms);
        prescription.setDoctorNotes(doctorNotes);
        prescription.setStatus(Prescription.PrescriptionStatus.ACTIVE);
        
        // Set medicines
        if (medicines != null) {
            for (PrescriptionMedicine medicine : medicines) {
                medicine.setPrescription(prescription);
            }
            prescription.setMedicines(medicines);
        }

        return prescriptionRepository.save(prescription);
    }

    public Prescription uploadPrescriptionFile(Long prescriptionId, MultipartFile file) {
        Optional<Prescription> prescriptionOpt = prescriptionRepository.findById(prescriptionId);
        if (!prescriptionOpt.isPresent()) {
            throw new RuntimeException("Prescription not found with id: " + prescriptionId);
        }

        try {
            String fileUrl = fileUploadService.uploadPrescriptionFile(file, prescriptionId);
            
            Prescription prescription = prescriptionOpt.get();
            prescription.setPrescriptionImageUrl(fileUrl);
            prescription.setOriginalFileName(file.getOriginalFilename());
            
            return prescriptionRepository.save(prescription);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload prescription file: " + e.getMessage());
        }
    }

    public List<Prescription> getPatientPrescriptions(Long patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (!patientOpt.isPresent()) {
            throw new RuntimeException("Patient not found with id: " + patientId);
        }
        
        return prescriptionRepository.findByPatientOrderByCreatedAtDesc(patientOpt.get());
    }

    public List<Prescription> getDoctorPrescriptions(Long doctorId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (!doctorOpt.isPresent()) {
            throw new RuntimeException("Doctor not found with id: " + doctorId);
        }
        
        return prescriptionRepository.findByDoctorOrderByCreatedAtDesc(doctorOpt.get());
    }

    public List<Prescription> getActivePrescriptions(Long patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (!patientOpt.isPresent()) {
            throw new RuntimeException("Patient not found with id: " + patientId);
        }
        
        return prescriptionRepository.findActiveValidPrescriptions(
            patientOpt.get(), 
            Prescription.PrescriptionStatus.ACTIVE, 
            LocalDateTime.now()
        );
    }

    public Optional<Prescription> getPrescriptionByNumber(String prescriptionNumber) {
        return prescriptionRepository.findByPrescriptionNumber(prescriptionNumber);
    }

    public Prescription updatePrescriptionStatus(Long prescriptionId, Prescription.PrescriptionStatus status) {
        Optional<Prescription> prescriptionOpt = prescriptionRepository.findById(prescriptionId);
        if (!prescriptionOpt.isPresent()) {
            throw new RuntimeException("Prescription not found with id: " + prescriptionId);
        }

        Prescription prescription = prescriptionOpt.get();
        prescription.setStatus(status);
        return prescriptionRepository.save(prescription);
    }

    public void expireOldPrescriptions() {
        List<Prescription> expiredPrescriptions = prescriptionRepository
            .findByValidUntilBeforeAndStatus(LocalDateTime.now(), Prescription.PrescriptionStatus.ACTIVE);
        
        for (Prescription prescription : expiredPrescriptions) {
            prescription.setStatus(Prescription.PrescriptionStatus.EXPIRED);
        }
        
        prescriptionRepository.saveAll(expiredPrescriptions);
    }

    public Optional<Prescription> getPrescriptionById(Long prescriptionId) {
        return prescriptionRepository.findById(prescriptionId);
    }

    public Long getPatientPrescriptionCount(Long patientId, Prescription.PrescriptionStatus status) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (!patientOpt.isPresent()) {
            return 0L;
        }
        
        return prescriptionRepository.countByPatientAndStatus(patientOpt.get(), status);
    }

    public Long getDoctorPrescriptionCount(Long doctorId, Prescription.PrescriptionStatus status) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (!doctorOpt.isPresent()) {
            return 0L;
        }
        
        return prescriptionRepository.countByDoctorAndStatus(doctorOpt.get(), status);
    }
}
