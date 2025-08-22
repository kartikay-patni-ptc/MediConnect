package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

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

        // Initialize order items list
        List<OrderItem> orderItems = new ArrayList<>();

        // Create order items from prescription medicines
        for (PrescriptionMedicine medicine : prescription.getMedicines()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setPrescriptionMedicine(medicine);
            orderItem.setMedicineName(medicine.getMedicineName());
            orderItem.setDosage(medicine.getDosage());
            orderItem.setQuantityRequested(medicine.getQuantity());
            orderItem.setStatus(OrderItem.ItemStatus.PENDING);
            orderItems.add(orderItem);
        }

        // Set the order items
        order.setOrderItems(orderItems);

        // Calculate basic amounts (can be enhanced with actual pricing logic)
        BigDecimal baseAmount = new BigDecimal("100.00"); // Base price per medicine
        BigDecimal totalAmount = baseAmount.multiply(new BigDecimal(orderItems.size()));
        BigDecimal deliveryFee = new BigDecimal("50.00"); // Fixed delivery fee
        BigDecimal finalAmount = totalAmount.add(deliveryFee);

        order.setTotalAmount(totalAmount);
        order.setDeliveryFee(deliveryFee);
        order.setFinalAmount(finalAmount);

        MedicineOrder savedOrder = medicineOrderRepository.save(order);

        // Try to find and assign a pharmacy
        try {
            MedicineOrder assignedOrder = assignPharmacy(savedOrder.getId(), deliveryPincode);
            return assignedOrder; // Return the updated order with pharmacy assigned
        } catch (Exception e) {
            // Log error but don't fail the order creation
            System.err.println("Failed to auto-assign pharmacy: " + e.getMessage());
            // Return the order without pharmacy - it will be assigned later
            return savedOrder;
        }
    }

    public MedicineOrder assignPharmacy(Long orderId, String pincode) {
        Optional<MedicineOrder> orderOpt = medicineOrderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        MedicineOrder order = orderOpt.get();
        
        // First try to find nearby pharmacies by pincode
        List<PharmacyStore> nearbyPharmacies = pharmacyMatchingService.findNearbyPharmacies(pincode, 10.0); // 10km radius
        
        if (nearbyPharmacies.isEmpty()) {
            // Fallback: get any available pharmacy
            List<PharmacyStore> allPharmacies = pharmacyStoreRepository.findAll();
            if (allPharmacies.isEmpty()) {
                throw new RuntimeException("No pharmacies available in the system");
            }
            // Assign to the first available pharmacy
            PharmacyStore assignedPharmacy = allPharmacies.get(0);
            order.setPharmacy(assignedPharmacy);
            order.setStatus(MedicineOrder.OrderStatus.PHARMACY_ASSIGNED);
            System.out.println("Assigned order " + orderId + " to pharmacy: " + assignedPharmacy.getName());
        } else {
            // Assign to the nearest pharmacy
            PharmacyStore assignedPharmacy = nearbyPharmacies.get(0);
            order.setPharmacy(assignedPharmacy);
            order.setStatus(MedicineOrder.OrderStatus.PHARMACY_ASSIGNED);
            System.out.println("Assigned order " + orderId + " to nearby pharmacy: " + assignedPharmacy.getName());
        }

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

    public List<MedicineOrder> getPharmacyOrdersByUserId(Long pharmacyUserId) {
        Optional<PharmacyStore> pharmacyOpt = pharmacyStoreRepository.findByUserId(pharmacyUserId);
        if (!pharmacyOpt.isPresent()) {
            throw new RuntimeException("Pharmacy not found for user id: " + pharmacyUserId);
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

    // New methods for pharmacy order management
    public MedicineOrder acceptOrder(Long orderId) {
        Optional<MedicineOrder> orderOpt = medicineOrderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        MedicineOrder order = orderOpt.get();
        order.setStatus(MedicineOrder.OrderStatus.ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());
        order.setExpectedDeliveryTime(LocalDateTime.now().plusHours(2)); // Default 2 hours

        return medicineOrderRepository.save(order);
    }

    public MedicineOrder rejectOrder(Long orderId, String rejectionReason) {
        Optional<MedicineOrder> orderOpt = medicineOrderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        MedicineOrder order = orderOpt.get();
        order.setStatus(MedicineOrder.OrderStatus.REJECTED);
        order.setRejectionReason(rejectionReason);

        return medicineOrderRepository.save(order);
    }

    public List<MedicineOrderDto> getPharmacyOrdersByUserIdAsDto(Long pharmacyUserId) {
        Optional<PharmacyStore> pharmacyOpt = pharmacyStoreRepository.findByUserId(pharmacyUserId);
        if (!pharmacyOpt.isPresent()) {
            throw new RuntimeException("Pharmacy not found for user id: " + pharmacyUserId);
        }
        
        List<MedicineOrder> orders = medicineOrderRepository.findByPharmacyWithPatientAndPrescriptionOrderByCreatedAtDesc(pharmacyOpt.get());
        return orders.stream()
                .map(this::convertToDto)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<MedicineOrderDto> getPatientOrdersAsDto(Long patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (!patientOpt.isPresent()) {
            throw new RuntimeException("Patient not found with id: " + patientId);
        }
        
        List<MedicineOrder> orders = medicineOrderRepository.findByPatientWithPatientAndPrescriptionOrderByCreatedAtDesc(patientOpt.get());
        return orders.stream()
                .map(this::convertToDto)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<MedicineOrderDto> getPendingOrdersAsDto() {
        List<MedicineOrder> orders = medicineOrderRepository.findByStatusWithPatientAndPrescriptionOrderByCreatedAtAsc(MedicineOrder.OrderStatus.PENDING);
        return orders.stream()
                .map(this::convertToDto)
                .collect(java.util.stream.Collectors.toList());
    }

    public MedicineOrderDto getOrderByIdAsDto(Long orderId) {
        Optional<MedicineOrder> orderOpt = medicineOrderRepository.findByIdWithPatientAndPrescription(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }
        return convertToDto(orderOpt.get());
    }

    public MedicineOrderDto getOrderByNumberAsDto(String orderNumber) {
        Optional<MedicineOrder> orderOpt = medicineOrderRepository.findByOrderNumberWithPatientAndPrescription(orderNumber);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Order not found with number: " + orderNumber);
        }
        return convertToDto(orderOpt.get());
    }

    public MedicineOrderDto convertToDto(MedicineOrder order) {
        MedicineOrderDto dto = new MedicineOrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setStatus(order.getStatus());
        dto.setOrderType(order.getOrderType());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setDeliveryPincode(order.getDeliveryPincode());
        dto.setPatientPhoneNumber(order.getPatientPhoneNumber());
        dto.setSpecialInstructions(order.getSpecialInstructions());
        dto.setPharmacyNotes(order.getPharmacyNotes());
        dto.setRejectionReason(order.getRejectionReason());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setAcceptedAt(order.getAcceptedAt());
        dto.setExpectedDeliveryTime(order.getExpectedDeliveryTime());

        // Convert patient to DTO (excluding sensitive user data)
        if (order.getPatient() != null) {
            PatientDto patientDto = new PatientDto();
            patientDto.setId(order.getPatient().getId());
            patientDto.setFirstName(order.getPatient().getFirstName());
            patientDto.setLastName(order.getPatient().getLastName());
            patientDto.setPhoneNumber(order.getPatient().getPhoneNumber());
            patientDto.setEmail(order.getPatient().getEmail());
            patientDto.setDateOfBirth(order.getPatient().getDateOfBirth());
            patientDto.setGender(order.getPatient().getGender());
            patientDto.setAddress(order.getPatient().getAddress());
            patientDto.setEmergencyContact(order.getPatient().getEmergencyContact());
            patientDto.setMedicalHistory(order.getPatient().getMedicalHistory());
            dto.setPatient(patientDto);
        }

        // Convert pharmacy to DTO (excluding sensitive user data)
        if (order.getPharmacy() != null) {
            PharmacyStoreDto pharmacyDto = new PharmacyStoreDto();
            pharmacyDto.setId(order.getPharmacy().getId());
            pharmacyDto.setName(order.getPharmacy().getName());
            pharmacyDto.setOwnerName(order.getPharmacy().getOwnerName());
            pharmacyDto.setLicenseNumber(order.getPharmacy().getLicenseNumber());
            pharmacyDto.setPhoneNumber(order.getPharmacy().getPhoneNumber());
            pharmacyDto.setEmail(order.getPharmacy().getEmail());
            pharmacyDto.setAddress(order.getPharmacy().getAddress());
            pharmacyDto.setDescription(order.getPharmacy().getDescription());
            pharmacyDto.setLatitude(order.getPharmacy().getLatitude());
            pharmacyDto.setLongitude(order.getPharmacy().getLongitude());
            dto.setPharmacy(pharmacyDto);
        }

        // Convert prescription to DTO (excluding sensitive data)
        if (order.getPrescription() != null) {
            PrescriptionDto prescriptionDto = new PrescriptionDto();
            prescriptionDto.setId(order.getPrescription().getId());
            prescriptionDto.setPrescriptionNumber(order.getPrescription().getPrescriptionNumber());
            prescriptionDto.setPrescribedDate(order.getPrescription().getIssuedDate());
            prescriptionDto.setDiagnosis(order.getPrescription().getDiagnosis());
            prescriptionDto.setNotes(order.getPrescription().getDoctorNotes());
            prescriptionDto.setStatus(order.getPrescription().getStatus().toString());
            dto.setPrescription(prescriptionDto);
        }

        // Convert order items to DTOs
        if (order.getOrderItems() != null) {
            List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                    .map(item -> {
                        OrderItemDto itemDto = new OrderItemDto();
                        itemDto.setId(item.getId());
                        itemDto.setMedicineName(item.getMedicineName());
                        itemDto.setDosage(item.getDosage());
                        itemDto.setQuantity(item.getQuantityRequested());
                        itemDto.setUnitPrice(item.getUnitPrice());
                        itemDto.setTotalPrice(item.getTotalPrice());
                        itemDto.setInstructions(item.getSubstitutionNote());
                        return itemDto;
                    })
                    .collect(java.util.stream.Collectors.toList());
            dto.setOrderItems(orderItemDtos);
        }

        // Convert delivery tracking to DTO
        if (order.getDeliveryTracking() != null) {
            DeliveryTrackingDto trackingDto = new DeliveryTrackingDto();
            trackingDto.setId(order.getDeliveryTracking().getId());
            trackingDto.setStatus(order.getDeliveryTracking().getDeliveryStatus().toString());
            trackingDto.setCurrentLocation(order.getDeliveryTracking().getCurrentLatitude() + "," + order.getDeliveryTracking().getCurrentLongitude());
            trackingDto.setEstimatedDeliveryTime(order.getDeliveryTracking().getEstimatedDeliveryTime().toString());
            trackingDto.setLastUpdated(order.getDeliveryTracking().getUpdatedAt());
            trackingDto.setDeliveryPartnerName(order.getDeliveryTracking().getDeliveryPartnerName());
            trackingDto.setDeliveryPartnerPhone(order.getDeliveryTracking().getDeliveryPartnerPhone());
            dto.setDeliveryTracking(trackingDto);
        }

        // Convert payment to DTO
        if (order.getPayment() != null) {
            OrderPaymentDto paymentDto = new OrderPaymentDto();
            paymentDto.setId(order.getPayment().getId());
            paymentDto.setPaymentMethod(order.getPayment().getPaymentMethod().toString());
            paymentDto.setTransactionId(order.getPayment().getTransactionId());
            paymentDto.setAmount(order.getPayment().getAmount());
            paymentDto.setStatus(order.getPayment().getPaymentStatus().toString());
            paymentDto.setPaymentDate(order.getPayment().getPaidAt());
            dto.setPayment(paymentDto);
        }

        return dto;
    }
}
