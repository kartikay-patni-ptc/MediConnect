package com.example.demo.repository;

import com.example.demo.model.MedicineOrder;
import com.example.demo.model.Patient;
import com.example.demo.model.PharmacyStore;
import com.example.demo.model.MedicineOrder.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineOrderRepository extends JpaRepository<MedicineOrder, Long> {

    List<MedicineOrder> findByPatientOrderByCreatedAtDesc(Patient patient);
    
    List<MedicineOrder> findByPharmacyOrderByCreatedAtDesc(PharmacyStore pharmacy);
    
    List<MedicineOrder> findByPatientAndStatusOrderByCreatedAtDesc(Patient patient, OrderStatus status);
    
    List<MedicineOrder> findByPharmacyAndStatusOrderByCreatedAtDesc(PharmacyStore pharmacy, OrderStatus status);
    
    Optional<MedicineOrder> findByOrderNumber(String orderNumber);
    
    @Query("SELECT o FROM MedicineOrder o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<MedicineOrder> findByStatusOrderByCreatedAtAsc(@Param("status") OrderStatus status);
    
    @Query("SELECT o FROM MedicineOrder o WHERE o.pharmacy = :pharmacy AND o.createdAt BETWEEN :startDate AND :endDate")
    List<MedicineOrder> findByPharmacyAndDateRange(@Param("pharmacy") PharmacyStore pharmacy, 
                                                  @Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(o) FROM MedicineOrder o WHERE o.pharmacy = :pharmacy AND o.status = :status")
    Long countByPharmacyAndStatus(@Param("pharmacy") PharmacyStore pharmacy, @Param("status") OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM MedicineOrder o WHERE o.patient = :patient AND o.status = :status")
    Long countByPatientAndStatus(@Param("patient") Patient patient, @Param("status") OrderStatus status);
    
    @Query("SELECT o FROM MedicineOrder o WHERE o.status IN :statuses AND o.pharmacy IS NULL ORDER BY o.createdAt ASC")
    List<MedicineOrder> findUnassignedOrders(@Param("statuses") List<OrderStatus> statuses);
    
    @Query("SELECT SUM(o.finalAmount) FROM MedicineOrder o WHERE o.pharmacy = :pharmacy AND o.status = :status AND o.createdAt BETWEEN :startDate AND :endDate")
    Double getTotalRevenueByPharmacyAndDateRange(@Param("pharmacy") PharmacyStore pharmacy, 
                                               @Param("status") OrderStatus status,
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM MedicineOrder o JOIN FETCH o.patient JOIN FETCH o.prescription WHERE o.pharmacy = :pharmacy ORDER BY o.createdAt DESC")
    List<MedicineOrder> findByPharmacyWithPatientAndPrescriptionOrderByCreatedAtDesc(@Param("pharmacy") PharmacyStore pharmacy);
    
    @Query("SELECT o FROM MedicineOrder o JOIN FETCH o.patient JOIN FETCH o.prescription WHERE o.patient = :patient ORDER BY o.createdAt DESC")
    List<MedicineOrder> findByPatientWithPatientAndPrescriptionOrderByCreatedAtDesc(@Param("patient") Patient patient);
    
    @Query("SELECT o FROM MedicineOrder o JOIN FETCH o.patient JOIN FETCH o.prescription WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<MedicineOrder> findByStatusWithPatientAndPrescriptionOrderByCreatedAtAsc(@Param("status") OrderStatus status);
    
    @Query("SELECT o FROM MedicineOrder o JOIN FETCH o.patient JOIN FETCH o.prescription WHERE o.id = :orderId")
    Optional<MedicineOrder> findByIdWithPatientAndPrescription(@Param("orderId") Long orderId);
    
    @Query("SELECT o FROM MedicineOrder o JOIN FETCH o.patient JOIN FETCH o.prescription WHERE o.orderNumber = :orderNumber")
    Optional<MedicineOrder> findByOrderNumberWithPatientAndPrescription(@Param("orderNumber") String orderNumber);
}
