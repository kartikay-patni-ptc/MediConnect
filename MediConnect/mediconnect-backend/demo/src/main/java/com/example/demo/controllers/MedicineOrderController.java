package com.example.demo.controllers;

import com.example.demo.model.*;
import com.example.demo.service.MedicineOrderService;
import com.example.demo.service.PharmacyMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/medicine-orders")
@CrossOrigin(origins = "*")
public class MedicineOrderController {

    @Autowired
    private MedicineOrderService medicineOrderService;

    @Autowired
    private PharmacyMatchingService pharmacyMatchingService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            MedicineOrder order = medicineOrderService.createOrder(
                request.getPrescriptionId(),
                request.getPatientId(),
                request.getDeliveryAddress(),
                request.getDeliveryPincode(),
                request.getSpecialInstructions()
            );

            MedicineOrderDto orderDto = medicineOrderService.convertToDto(order);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Medicine order created successfully");
            response.put("order", orderDto);
            response.put("orderNumber", order.getOrderNumber());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create order: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientOrders(@PathVariable Long patientId) {
        try {
            List<MedicineOrderDto> orders = medicineOrderService.getPatientOrdersAsDto(patientId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orders);
            response.put("count", orders.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get patient orders: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/pharmacy/{pharmacyUserId}")
    public ResponseEntity<?> getPharmacyOrders(@PathVariable Long pharmacyUserId) {
        try {
            List<MedicineOrderDto> orders = medicineOrderService.getPharmacyOrdersByUserIdAsDto(pharmacyUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orders);
            response.put("count", orders.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get pharmacy orders: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/pharmacy/{pharmacyUserId}/statistics")
    public ResponseEntity<?> getPharmacyOrderStatistics(@PathVariable Long pharmacyUserId) {
        try {
            List<MedicineOrderDto> orders = medicineOrderService.getPharmacyOrdersByUserIdAsDto(pharmacyUserId);
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", orders.size());
            stats.put("pendingOrders", orders.stream().filter(o -> o.getStatus() == MedicineOrder.OrderStatus.PENDING || o.getStatus() == MedicineOrder.OrderStatus.PHARMACY_ASSIGNED).count());
            stats.put("acceptedOrders", orders.stream().filter(o -> o.getStatus() == MedicineOrder.OrderStatus.ACCEPTED).count());
            stats.put("preparingOrders", orders.stream().filter(o -> o.getStatus() == MedicineOrder.OrderStatus.PREPARING).count());
            stats.put("readyOrders", orders.stream().filter(o -> o.getStatus() == MedicineOrder.OrderStatus.READY_FOR_PICKUP).count());
            stats.put("deliveredOrders", orders.stream().filter(o -> o.getStatus() == MedicineOrder.OrderStatus.DELIVERED).count());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get statistics: " + e.getMessage());
        }
    }

    @GetMapping("/pharmacy/{pharmacyUserId}/recent")
    public ResponseEntity<?> getRecentOrders(@PathVariable Long pharmacyUserId, @RequestParam(defaultValue = "10") int limit) {
        try {
            List<MedicineOrderDto> orders = medicineOrderService.getPharmacyOrdersByUserIdAsDto(pharmacyUserId);
            // Sort by creation date and limit results
            List<MedicineOrderDto> recentOrders = orders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(recentOrders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get recent orders: " + e.getMessage());
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingOrders() {
        try {
            List<MedicineOrderDto> orders = medicineOrderService.getPendingOrdersAsDto();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orders);
            response.put("count", orders.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get pending orders: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        try {
            MedicineOrderDto order = medicineOrderService.getOrderByIdAsDto(orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("order", order);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get order: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<?> getOrderByNumber(@PathVariable String orderNumber) {
        try {
            MedicineOrderDto order = medicineOrderService.getOrderByNumberAsDto(orderNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("order", order);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get order: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{orderId}/accept")
    public ResponseEntity<?> acceptOrder(@PathVariable Long orderId, @RequestBody AcceptOrderRequest request) {
        try {
            MedicineOrder order = medicineOrderService.acceptOrder(orderId, request.getPharmacyId());

            MedicineOrderDto orderDto = medicineOrderService.convertToDto(order);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order accepted successfully");
            response.put("order", orderDto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to accept order: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }



    @PostMapping("/{orderId}/reject")
    public ResponseEntity<?> rejectOrder(@PathVariable Long orderId, @RequestBody RejectOrderRequest request) {
        try {
            MedicineOrder order = medicineOrderService.rejectOrder(
                orderId, 
                request.getPharmacyId(), 
                request.getRejectionReason()
            );

            MedicineOrderDto orderDto = medicineOrderService.convertToDto(order);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order rejected successfully");
            response.put("order", orderDto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to reject order: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestBody UpdateStatusRequest request) {
        try {
            MedicineOrder order = medicineOrderService.updateOrderStatus(orderId, request.getStatus());

            MedicineOrderDto orderDto = medicineOrderService.convertToDto(order);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order status updated successfully");
            response.put("order", orderDto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update order status: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/nearby-pharmacies")
    public ResponseEntity<?> getNearbyPharmacies(
            @RequestParam String pincode,
            @RequestParam(defaultValue = "10.0") double radiusKm) {
        try {
            List<PharmacyStore> pharmacies = pharmacyMatchingService.findNearbyPharmacies(pincode, radiusKm);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pharmacies", pharmacies);
            response.put("count", pharmacies.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get nearby pharmacies: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/delivery-estimate")
    public ResponseEntity<?> getDeliveryEstimate(
            @RequestParam String pincode,
            @RequestParam Long pharmacyId) {
        try {
            double distance = pharmacyMatchingService.getDistanceToPharmacy(pincode, pharmacyId);
            if (distance < 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Unable to calculate distance to pharmacy");
                return ResponseEntity.badRequest().body(response);
            }

            int estimatedTime = pharmacyMatchingService.getEstimatedDeliveryTime(distance);
            double deliveryFee = pharmacyMatchingService.calculateDeliveryFee(distance);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("distance", distance);
            response.put("estimatedTimeMinutes", estimatedTime);
            response.put("deliveryFee", deliveryFee);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get delivery estimate: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // DTOs
    public static class CreateOrderRequest {
        private Long prescriptionId;
        private Long patientId;
        private String deliveryAddress;
        private String deliveryPincode;
        private String specialInstructions;

        // Getters and setters
        public Long getPrescriptionId() { return prescriptionId; }
        public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }
        
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }
        
        public String getDeliveryAddress() { return deliveryAddress; }
        public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
        
        public String getDeliveryPincode() { return deliveryPincode; }
        public void setDeliveryPincode(String deliveryPincode) { this.deliveryPincode = deliveryPincode; }
        
        public String getSpecialInstructions() { return specialInstructions; }
        public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    }

    public static class AcceptOrderRequest {
        private Long pharmacyId;

        public Long getPharmacyId() { return pharmacyId; }
        public void setPharmacyId(Long pharmacyId) { this.pharmacyId = pharmacyId; }
    }

    public static class RejectOrderRequest {
        private Long pharmacyId;
        private String rejectionReason;

        public Long getPharmacyId() { return pharmacyId; }
        public void setPharmacyId(Long pharmacyId) { this.pharmacyId = pharmacyId; }
        
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }

    public static class UpdateStatusRequest {
        private MedicineOrder.OrderStatus status;

        public MedicineOrder.OrderStatus getStatus() { return status; }
        public void setStatus(MedicineOrder.OrderStatus status) { this.status = status; }
    }
}
