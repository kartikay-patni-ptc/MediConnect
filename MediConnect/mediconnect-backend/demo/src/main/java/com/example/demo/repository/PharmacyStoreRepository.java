package com.example.demo.repository;

import com.example.demo.model.PharmacyStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PharmacyStoreRepository extends JpaRepository<PharmacyStore, Long> {
    Optional<PharmacyStore> findByUserId(Long userId);
}
