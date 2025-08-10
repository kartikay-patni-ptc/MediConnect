package com.example.demo.repository;

import com.example.demo.model.PharmacyStore;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PharmacyStoreRepository extends JpaRepository<PharmacyStore, Long> {
    Optional<PharmacyStore> findByUser(User user);
    Optional<PharmacyStore> findByUserId(Long userId);
}
