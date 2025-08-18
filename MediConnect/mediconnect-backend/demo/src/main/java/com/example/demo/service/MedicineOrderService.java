package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MedicineOrderService {

    @Autowired
    private MedicineOrderRepository medicineOrderRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PharmacyStoreRepository pharmacyStoreRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PharmacyMatchingService pharmacyMatchingService;

    public MedicineOrder createOrder(Long prescriptionId, Long patientId, String deliveryAddress, 
                                   String deliveryPincode, String specialInstructions) {
        
        Optional<Prescription> prescriptionOpt = prescriptionRepository.findById(prescriptionId);
        if (!prescriptionOpt.isPresent()) {
            throw new RuntimeException("Prescription not found with id: " + prescriptionId);
        }

        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (!patientOpt.isPresent()) {
            throw new RuntimeException("Patient not found with id: " + patientId);
        }

        Prescription prescription = prescriptionOpt.get();
        Patient patient = patientOpt.get();

        // Validate prescription is active and valid
        if (prescription.getStatus() != Prescription.PrescriptionStatus.ACTIVE) {
            throw new RuntimeException("Prescription is not active");
        }

        if (prescription.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Prescription has expired");
        }

        MedicineOrder order = new MedicineOrder();
        order.setPrescription(prescription);
        order.setPatient(patient);
        order.setStatus(MedicineOrder.OrderStatus.PENDING);
        order.setOrderType(MedicineOrder.OrderType.DELIVERY);
        order.setDeliveryAddress(deliveryAddress);
        order.setDeliveryPincode(deliveryPincode);
        order.setPatientPhoneNumber(patient.getPhoneNumber());
        order.setSpecialInstructions(specialInstructions);

        // Create order items from prescription medicines
        for (PrescriptionMedicine medicine : prescription.getMedicines()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setPrescriptionMedicine(medicine);
            orderItem.setMedicineName(medicine.getMedicineName());
            orderItem.setDosage(medicine.getDosage());
            orderItem.setQuantityRequested(medicine.getQuantity());
            orderItem.setStatus(OrderItem.ItemStatus.PENDING);
        }

        MedicineOrder savedOrder = medicineOrderRepository.save(order);

        // Try to find and assign a pharmacy
        try {
            assignPharmacy(savedOrder.getId(), deliveryPincode);
        } catch (Exception e) {
            // Log error but don't fail the order creation
            System.err.println("Failed to auto-assign pharmacy: " + e.getMessage());
        }

        return savedOrder;
    }

    public MedicineOrder assignPharmacy(Long orderId, String pincode) {
        Optional<MedicineOrder> orderOpt = medicineOrderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        MedicineOrder order = orderOpt.get();
        
        // Find nearest available pharmacy
        List<PharmacyStore> nearbyPharmacies = pharmacyMatchingService.findNearbyPharmacies(pincode, 10.0); // 10km radius
        
        if (nearbyPharmacies.isEmpty()) {
            throw new RuntimeException("No pharmacies available in the area");
        }

        // Assign to the first available pharmacy (can be enhanced with more logic)
        PharmacyStore assignedPharmacy = nearbyPharmacies.get(0);
        order.setPharmacy(assignedPharmacy);
        order.setStatus(MedicineOrder.OrderStatus.PHARMACY_ASSIGNED);

        return medicineOrderRepository.save(order);
    }

    public MedicineOrder acceptOrder(Long orderId, Long pharmacyId) {
        Optional<MedicineOrder> orderOpt = medicineOrderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        MedicineOrder order = orderOpt.get();
        
        // Verify pharmacy ownership
        if (!order.getPharmacy().getId().equals(pharmacyId)) {
            throw new RuntimeException("Pharmacy not authorized to accept this order");
        }

        order.setStatus(MedicineOrder.OrderStatus.ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());
        order.setExpectedDeliveryTime(LocalDateTime.now().plusHours(2)); // Default 2 hours

        return medicineOrderRepository.save(order);
    }

    public MedicineOrder rejectOrder(Long orderId, Long pharmacyId, String rejectionReason) {
        Optional<MedicineOrder> orderOpt = medicineOrderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        MedicineOrder order = orderOpt.get();
        
        // Verify pharmacy ownership
        if (!order.getPharmacy().getId().equals(pharmacyId)) {
            throw new RuntimeException("Pharmacy not authorized to reject this order");
        }

        order.setStatus(MedicineOrder.OrderStatus.REJECTED);
        order.setRejectionReason(rejectionReason);
        order.setPharmacy(null); // Remove pharmacy assignment

        MedicineOrder savedOrder = medicineOrderRepository.save(order);

        // Try to reassign to another pharmacy
        try {
            assignPharmacy(savedOrder.getId(), savedOrder.getDeliveryPincode());
        } catch (Exception e) {
            // If no other pharmacy available, keep as rejected
            System.err.println("Failed to reassign pharmacy: " + e.getMessage());
        }

        return savedOrder;
    }

    public MedicineOrder updateOrderStatus(Long orderId, MedicineOrder.OrderStatus status) {
        Optional<MedicineOrder> orderOpt = medicineOrderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        MedicineOrder order = orderOpt.get();
        order.setStatus(status);
        
        return medicineOrderRepository.save(order);
    }

    public List<MedicineOrder> getPatientOrders(Long patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (!patientOpt.isPresent()) {
            throw new RuntimeException("Patient not found with id: " + patientId);
        }
        
        return medicineOrderRepository.findByPatientOrderByCreatedAtDesc(patientOpt.get());
    }

    public List<MedicineOrder> getPharmacyOrders(Long pharmacyId) {
        Optional<PharmacyStore> pharmacyOpt = pharmacyStoreRepository.findById(pharmacyId);
        if (!pharmacyOpt.isPresent()) {
            throw new RuntimeException("Pharmacy not found with id: " + pharmacyId);
        }
        
        return medicineOrderRepository.findByPharmacyOrderByCreatedAtDesc(pharmacyOpt.get());
    }

    public List<MedicineOrder> getPendingOrders() {
        return medicineOrderRepository.findByStatusOrderByCreatedAtAsc(MedicineOrder.OrderStatus.PENDING);
    }

    public Optional<MedicineOrder> getOrderByNumber(String orderNumber) {
        return medicineOrderRepository.findByOrderNumber(orderNumber);
    }

    public Optional<MedicineOrder> getOrderById(Long orderId) {
        return medicineOrderRepository.findById(orderId);
    }

    public Long getPharmacyOrderCount(Long pharmacyId, MedicineOrder.OrderStatus status) {
        Optional<PharmacyStore> pharmacyOpt = pharmacyStoreRepository.findById(pharmacyId);
        if (!pharmacyOpt.isPresent()) {
            return 0L;
        }
        
        return medicineOrderRepository.countByPharmacyAndStatus(pharmacyOpt.get(), status);
    }

    public Long getPatientOrderCount(Long patientId, MedicineOrder.OrderStatus status) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (!patientOpt.isPresent()) {
            return 0L;
        }
        
        return medicineOrderRepository.countByPatientAndStatus(patientOpt.get(), status);
    }
}
