package com.example.demo.service;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.model.PharmacyStoreDto;
import com.example.demo.model.PharmacyStore;
import com.example.demo.repository.PharmacyStoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PharmacyStoreService {

    @Autowired
    private PharmacyStoreRepository repository;
    @Autowired
    private GeocodingService geocodingService;
    @Autowired
    private UserService userService;

    public PharmacyStore createPharmacyStore(PharmacyStoreDto dto) {
        System.out.println("=== CREATING PHARMACY STORE ===");
        System.out.println("User ID: " + dto.getId());

        // Find the user by ID
        User user = userService.findById(dto.getId());
        if (user == null) {
            throw new RuntimeException("User not found with id: " + dto.getId());
        }
        System.out.println("User found: " + user.getUsername());

        // Check if pharmacy store already exists for this user
        Optional<PharmacyStore> existingStore = repository.findByUser(user);
        if (existingStore.isPresent()) {
            throw new RuntimeException("Pharmacy store already exists for this user");
        }
        System.out.println("No existing store found, creating new one...");

        PharmacyStore store = new PharmacyStore();
        store.setName(dto.getName());
        store.setOwnerName(dto.getOwnerName());
        store.setLicenseNumber(dto.getLicenseNumber());
        store.setPhoneNumber(dto.getPhoneNumber());
        store.setEmail(dto.getEmail());
        store.setAddress(dto.getAddress());
        store.setDescription(dto.getDescription());

        // Set coordinates from DTO (if provided)
        double[] coordinates = geocodingService.getCoordinates(dto.getAddress());
        store.setLatitude(coordinates[0]);
        store.setLongitude(coordinates[1]);
        store.setUser(user);

        System.out.println("Saving pharmacy store...");
        PharmacyStore savedStore = repository.save(store);
        System.out.println("Pharmacy store saved with ID: " + savedStore.getId());
        return savedStore;
    }

    public List<PharmacyStore> getAllStores() {
        return repository.findAll();
    }

    public Optional<PharmacyStore> getStoreByUser(User user) {
        return repository.findByUser(user);
    }

    public Optional<PharmacyStore> getStoreByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    public Optional<PharmacyStore> getStoreById(Long id) {
        return repository.findById(id);
    }

    public PharmacyStore updateStore(Long id, PharmacyStoreDto dto) {
        Optional<PharmacyStore> existingStore = repository.findById(id);
        if (existingStore.isPresent()) {
            PharmacyStore store = existingStore.get();

            store.setName(dto.getName());
            store.setOwnerName(dto.getOwnerName());
            store.setLicenseNumber(dto.getLicenseNumber());
            store.setPhoneNumber(dto.getPhoneNumber());
            store.setEmail(dto.getEmail());
            store.setAddress(dto.getAddress());
            store.setDescription(dto.getDescription());

            // Update coordinates from DTO (if provided)
            double[] coordinates = geocodingService.getCoordinates(dto.getAddress());
            store.setLatitude(coordinates[0]);
            store.setLongitude(coordinates[1]);

            return repository.save(store);
        }
        throw new RuntimeException("Pharmacy store not found with id: " + id);
    }
}
