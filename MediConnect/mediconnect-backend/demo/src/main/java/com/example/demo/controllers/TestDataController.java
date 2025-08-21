package com.example.demo.controllers;

import com.example.demo.model.Doctor;
import com.example.demo.model.DoctorSlot;
import com.example.demo.model.User;
import com.example.demo.model.Patient;
import com.example.demo.model.Appointment;
import com.example.demo.model.MedicineOrder;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Prescription;
import com.example.demo.model.PrescriptionMedicine;
import com.example.demo.model.PharmacyStore;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.repository.DoctorSlotRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.MedicineOrderRepository;
import com.example.demo.repository.PrescriptionRepository;
import com.example.demo.repository.PharmacyStoreRepository;
import com.example.demo.utils.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestDataController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorSlotRepository doctorSlotRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MedicineOrderRepository medicineOrderRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PharmacyStoreRepository pharmacyStoreRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/init-data")
    public ResponseEntity<String> initializeTestData() {
        try {
            // Create test doctors
            createTestDoctors();
            return ResponseEntity.ok("Test data initialized successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error initializing test data: " + e.getMessage());
        }
    }

    @PostMapping("/init-appointments")
    public ResponseEntity<String> initializeAppointments() {
        try {
            // Create test appointments
            createTestAppointments();
            return ResponseEntity.ok("Test appointments created successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating appointments: " + e.getMessage());
        }
    }

    @PostMapping("/init-slots")
    public ResponseEntity<String> initializeSlots() {
        try {
            // Get existing doctors
            List<Doctor> doctors = doctorRepository.findAll();
            
            if (doctors.isEmpty()) {
                return ResponseEntity.badRequest().body("No doctors found. Please create doctors first.");
            }
            
            // Create slots for the next 7 days for all existing doctors
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 7; i++) {
                LocalDate slotDate = today.plusDays(i);
                
                // Morning slots (9 AM - 12 PM)
                for (int hour = 9; hour < 12; hour++) {
                    for (Doctor doctor : doctors) {
                        createSlot(doctor, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                    }
                }
                
                // Afternoon slots (2 PM - 5 PM)
                for (int hour = 14; hour < 17; hour++) {
                    for (Doctor doctor : doctors) {
                        createSlot(doctor, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                    }
                }
            }
            
            return ResponseEntity.ok("Slots initialized successfully for " + doctors.size() + " doctors!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error initializing slots: " + e.getMessage());
        }
    }

    @PostMapping("/init-medicine-orders")
    public ResponseEntity<String> initializeMedicineOrders() {
        try {
            // Create test medicine orders
            createTestMedicineOrders();
            return ResponseEntity.ok("Test medicine orders created successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating medicine orders: " + e.getMessage());
        }
    }

    @PostMapping("/init-complete-data")
    public ResponseEntity<String> initializeCompleteData() {
        try {
            // Create everything needed for medicine orders
            createTestPharmacyStores();
            createTestPrescriptions();
            createTestMedicineOrders();
            return ResponseEntity.ok("Complete test data created successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating complete test data: " + e.getMessage());
        }
    }

    private void createTestDoctors() {
        // Create test users for doctors (only if they don't exist)
        User doctorUser1 = userRepository.findByUsername("dr.smith").orElseGet(() -> {
            User user = new User();
            user.setUsername("dr.smith");
            user.setEmail("dr.smith@mediconnect.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.DOCTOR);
            return userRepository.save(user);
        });

        User doctorUser2 = userRepository.findByUsername("dr.johnson").orElseGet(() -> {
            User user = new User();
            user.setUsername("dr.johnson");
            user.setEmail("dr.johnson@mediconnect.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.DOCTOR);
            return userRepository.save(user);
        });

        User doctorUser3 = userRepository.findByUsername("dr.williams").orElseGet(() -> {
            User user = new User();
            user.setUsername("dr.williams");
            user.setEmail("dr.williams@mediconnect.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.DOCTOR);
            return userRepository.save(user);
        });

        // Create doctors
        Doctor doctor1 = new Doctor();
        doctor1.setFirstName("Dr. Sarah");
        doctor1.setLastName("Smith");
        doctor1.setSpecialization("Cardiology");
        doctor1.setLicenseNumber("CARD001");
        doctor1.setPhoneNumber("+1-555-0101");
        doctor1.setEmail("dr.smith@mediconnect.com");
        doctor1.setExperience(15);
        doctor1.setEducation("MD, Cardiology");
        doctor1.setHospital("City General Hospital");
        doctor1.setAddress("123 Medical Center Dr, City");
        doctor1.setDescription("Experienced cardiologist specializing in heart disease prevention and treatment.");
        doctor1.setIsVerified(true);
        doctor1.setRegistrationNumber("REG001");
        doctor1.setUser(doctorUser1);
        doctorRepository.save(doctor1);

        Doctor doctor2 = new Doctor();
        doctor2.setFirstName("Dr. Michael");
        doctor2.setLastName("Johnson");
        doctor2.setSpecialization("Dermatology");
        doctor2.setLicenseNumber("DERM001");
        doctor2.setPhoneNumber("+1-555-0102");
        doctor2.setEmail("dr.johnson@mediconnect.com");
        doctor2.setExperience(12);
        doctor2.setEducation("MD, Dermatology");
        doctor2.setHospital("Skin Care Clinic");
        doctor2.setAddress("456 Health Plaza, City");
        doctor2.setDescription("Board-certified dermatologist with expertise in skin conditions and cosmetic procedures.");
        doctor2.setIsVerified(true);
        doctor2.setRegistrationNumber("REG002");
        doctor2.setUser(doctorUser2);
        doctorRepository.save(doctor2);

        Doctor doctor3 = new Doctor();
        doctor3.setFirstName("Dr. Emily");
        doctor3.setLastName("Williams");
        doctor3.setSpecialization("Pediatrics");
        doctor3.setLicenseNumber("PED001");
        doctor3.setPhoneNumber("+1-555-0103");
        doctor3.setEmail("dr.williams@mediconnect.com");
        doctor3.setExperience(8);
        doctor3.setEducation("MD, Pediatrics");
        doctor3.setHospital("Children's Medical Center");
        doctor3.setAddress("789 Family Care Ave, City");
        doctor3.setDescription("Caring pediatrician dedicated to children's health and development.");
        doctor3.setIsVerified(true);
        doctor3.setRegistrationNumber("REG003");
        doctor3.setUser(doctorUser3);
        doctorRepository.save(doctor3);

        // Create slots for the next 7 days
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate slotDate = today.plusDays(i);
            
            // Morning slots (9 AM - 12 PM)
            for (int hour = 9; hour < 12; hour++) {
                createSlot(doctor1, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                createSlot(doctor2, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                createSlot(doctor3, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
            }
            
            // Afternoon slots (2 PM - 5 PM)
            for (int hour = 14; hour < 17; hour++) {
                createSlot(doctor1, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                createSlot(doctor2, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                createSlot(doctor3, slotDate, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
            }
        }
    }

    private void createSlot(Doctor doctor, LocalDate date, LocalTime startTime, LocalTime endTime) {
        DoctorSlot slot = new DoctorSlot();
        slot.setDoctor(doctor);
        slot.setStartTime(date.atTime(startTime));
        slot.setEndTime(date.atTime(endTime));
        slot.setAvailable(true);
        doctorSlotRepository.save(slot);
    }

    private void createTestAppointments() {
        // Get all doctors and patients
        List<Doctor> doctors = doctorRepository.findAll();
        List<Patient> patients = patientRepository.findAll();
        
        if (doctors.isEmpty() || patients.isEmpty()) {
            System.out.println("No doctors or patients found. Cannot create appointments.");
            return;
        }

        // Get some available slots
        List<DoctorSlot> availableSlots = doctorSlotRepository.findByAvailableTrue();
        
        if (availableSlots.isEmpty()) {
            System.out.println("No available slots found. Cannot create appointments.");
            return;
        }

        // Create some test appointments
        Doctor doctor1 = doctors.get(0);
        Patient patient1 = patients.size() > 0 ? patients.get(0) : null;
        Patient patient2 = patients.size() > 1 ? patients.get(1) : null;

        if (patient1 != null && availableSlots.size() > 0) {
            createAppointment(patient1, doctor1, availableSlots.get(0), "Patient experiencing chest pain", Appointment.Status.Pending);
        }
        
        if (patient2 != null && availableSlots.size() > 1) {
            createAppointment(patient2, doctor1, availableSlots.get(1), "Follow-up consultation", Appointment.Status.Confirmed);
        }

        if (patient1 != null && availableSlots.size() > 2) {
            createAppointment(patient1, doctor1, availableSlots.get(2), "Routine checkup", Appointment.Status.Completed);
        }

        System.out.println("Created test appointments successfully!");
    }

        private void createAppointment(Patient patient, Doctor doctor, DoctorSlot slot, String notes, Appointment.Status status) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setSlot(slot);
        appointment.setNotes(notes);
        appointment.setStatus(status);
        appointment.setCreatedAt(LocalDateTime.now());

        // Mark slot as unavailable
        slot.setAvailable(false);
        doctorSlotRepository.save(slot);

        appointmentRepository.save(appointment);
    }

    private void createTestMedicineOrders() {
        // Get existing patients and prescriptions
        List<Patient> patients = patientRepository.findAll();
        List<Prescription> prescriptions = prescriptionRepository.findAll();
        
        if (patients.isEmpty() || prescriptions.isEmpty()) {
            System.out.println("No patients or prescriptions found. Cannot create medicine orders.");
            return;
        }
        
        // Get some available pharmacies
        List<PharmacyStore> pharmacies = pharmacyStoreRepository.findAll();
        
        if (pharmacies.isEmpty()) {
            System.out.println("No pharmacies found. Cannot create medicine orders.");
            return;
        }
        
        // Create some test medicine orders
        Patient patient1 = patients.get(0);
        Prescription prescription1 = prescriptions.get(0);
        PharmacyStore pharmacy1 = pharmacies.get(0);
        
        if (patient1 != null && prescription1 != null && pharmacy1 != null) {
            createMedicineOrder(patient1, prescription1, pharmacy1, "123 Main St, City", "123456", "Please deliver in the morning");
        }
        
        if (patients.size() > 1 && prescriptions.size() > 1 && pharmacies.size() > 1) {
            Patient patient2 = patients.get(1);
            Prescription prescription2 = prescriptions.get(1);
            PharmacyStore pharmacy2 = pharmacies.get(1);
            
            createMedicineOrder(patient2, prescription2, pharmacy2, "456 Oak Ave, City", "654321", "Leave at front desk if no one answers");
        }
        
        System.out.println("Created test medicine orders successfully!");
    }
    
    private void createMedicineOrder(Patient patient, Prescription prescription, PharmacyStore pharmacy, String address, String pincode, String instructions) {
        MedicineOrder order = new MedicineOrder();
        order.setPatient(patient);
        order.setPrescription(prescription);
        order.setPharmacy(pharmacy);
        order.setStatus(MedicineOrder.OrderStatus.PENDING);
        order.setOrderType(MedicineOrder.OrderType.DELIVERY);
        order.setTotalAmount(new BigDecimal("500.00")); // Base amount
        order.setDeliveryFee(new BigDecimal("50.00"));
        order.setFinalAmount(new BigDecimal("550.00"));
        order.setDeliveryAddress(address);
        order.setDeliveryPincode(pincode);
        order.setPatientPhoneNumber(patient.getPhoneNumber());
        order.setSpecialInstructions(instructions);
        order.setCreatedAt(LocalDateTime.now());
        
        // Create order items based on prescription medicines
        List<OrderItem> orderItems = new ArrayList<>();
        for (PrescriptionMedicine medicine : prescription.getMedicines()) {
            OrderItem item = new OrderItem();
            item.setPrescriptionMedicine(medicine);
            item.setMedicineName(medicine.getMedicineName());
            item.setDosage(medicine.getDosage());
            item.setQuantityRequested(medicine.getQuantity());
            item.setStatus(OrderItem.ItemStatus.PENDING);
            orderItems.add(item);
        }
        
        order.setOrderItems(orderItems);
        medicineOrderRepository.save(order);
    }

    private void createTestPharmacyStores() {
        // Create test users for pharmacies
        User pharmacyUser1 = userRepository.findByUsername("pharmacy1").orElseGet(() -> {
            User user = new User();
            user.setUsername("pharmacy1");
            user.setEmail("pharmacy1@example.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.PHARMACIST);
            return userRepository.save(user);
        });

        User pharmacyUser2 = userRepository.findByUsername("pharmacy2").orElseGet(() -> {
            User user = new User();
            user.setUsername("pharmacy2");
            user.setEmail("pharmacy2@example.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.PHARMACIST);
            return userRepository.save(user);
        });

        // Create test pharmacy stores
        PharmacyStore pharmacy1 = new PharmacyStore();
        pharmacy1.setName("City Pharmacy");
        pharmacy1.setAddress("123 Main Street, City Center");
        pharmacy1.setOwnerName("John Smith");
        pharmacy1.setLicenseNumber("PHARM001");
        pharmacy1.setPhoneNumber("+1-555-0101");
        pharmacy1.setEmail("citypharmacy@example.com");
        pharmacy1.setDescription("Full-service pharmacy with delivery");
        pharmacy1.setLatitude(40.7128);
        pharmacy1.setLongitude(-74.0060);
        pharmacy1.setUser(pharmacyUser1);
        pharmacyStoreRepository.save(pharmacy1);

        PharmacyStore pharmacy2 = new PharmacyStore();
        pharmacy2.setName("Health Plus Pharmacy");
        pharmacy2.setAddress("456 Oak Avenue, Downtown");
        pharmacy2.setOwnerName("Jane Doe");
        pharmacy2.setLicenseNumber("PHARM002");
        pharmacy2.setPhoneNumber("+1-555-0102");
        pharmacy2.setEmail("healthplus@example.com");
        pharmacy2.setDescription("Specialized pharmacy for chronic conditions");
        pharmacy2.setLatitude(40.7589);
        pharmacy2.setLongitude(-73.9851);
        pharmacy2.setUser(pharmacyUser2);
        pharmacyStoreRepository.save(pharmacy2);

        System.out.println("Created test pharmacy stores successfully!");
    }

    private void createTestPrescriptions() {
        // Get existing patients and doctors
        List<Patient> patients = patientRepository.findAll();
        List<Doctor> doctors = doctorRepository.findAll();
        
        if (patients.isEmpty() || doctors.isEmpty()) {
            System.out.println("No patients or doctors found. Cannot create prescriptions.");
            return;
        }
        
        // Create test prescriptions
        Patient patient1 = patients.get(0);
        Doctor doctor1 = doctors.get(0);
        
        if (patient1 != null && doctor1 != null) {
            createPrescription(patient1, doctor1, "Hypertension", "High blood pressure", "Monitor BP daily");
        }
        
        if (patients.size() > 1 && doctors.size() > 1) {
            Patient patient2 = patients.get(1);
            Doctor doctor2 = doctors.get(1);
            createPrescription(patient2, doctor2, "Diabetes", "High blood sugar", "Check glucose levels");
        }
        
        System.out.println("Created test prescriptions successfully!");
    }
    
    private void createPrescription(Patient patient, Doctor doctor, String diagnosis, String symptoms, String notes) {
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setDiagnosis(diagnosis);
        prescription.setSymptoms(symptoms);
        prescription.setDoctorNotes(notes);
        prescription.setStatus(Prescription.PrescriptionStatus.ACTIVE);
        prescription.setIssuedDate(LocalDateTime.now());
        prescription.setValidUntil(LocalDateTime.now().plusMonths(3));
        prescription.setCreatedAt(LocalDateTime.now());
        
        // Create prescription medicines
        List<PrescriptionMedicine> medicines = new ArrayList<>();
        
        PrescriptionMedicine medicine1 = new PrescriptionMedicine();
        medicine1.setMedicineName("Amlodipine");
        medicine1.setGenericName("Amlodipine Besylate");
        medicine1.setDosage("5mg");
        medicine1.setFrequency("Once daily");
        medicine1.setDuration("30 days");
        medicine1.setQuantity(30);
        medicine1.setInstructions("Take with food");
        medicine1.setMedicineType(PrescriptionMedicine.MedicineType.PRESCRIPTION);
        medicines.add(medicine1);
        
        PrescriptionMedicine medicine2 = new PrescriptionMedicine();
        medicine2.setMedicineName("Metformin");
        medicine2.setGenericName("Metformin Hydrochloride");
        medicine2.setDosage("500mg");
        medicine2.setFrequency("Twice daily");
        medicine2.setDuration("30 days");
        medicine2.setQuantity(60);
        medicine2.setInstructions("Take with meals");
        medicine2.setMedicineType(PrescriptionMedicine.MedicineType.PRESCRIPTION);
        medicines.add(medicine2);
        
        prescription.setMedicines(medicines);
        prescriptionRepository.save(prescription);
    }
}
