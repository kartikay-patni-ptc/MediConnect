package com.example.demo.service;

import com.example.demo.model.PharmacyStore;
import com.example.demo.repository.PharmacyStoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PharmacyMatchingService {

    @Autowired
    private PharmacyStoreRepository pharmacyStoreRepository;

    @Autowired
    private GeocodingService geocodingService;

    public List<PharmacyStore> findNearbyPharmacies(String pincode, double radiusKm) {
        // Get coordinates for the pincode
        double[] coordinates = geocodingService.getCoordinates(pincode);
        double targetLat = coordinates[0];
        double targetLng = coordinates[1];

        // Get all pharmacies and filter by distance
        List<PharmacyStore> allPharmacies = pharmacyStoreRepository.findAll();
        
        return allPharmacies.stream()
            .filter(pharmacy -> pharmacy.getLatitude() != null && pharmacy.getLongitude() != null)
            .filter(pharmacy -> {
                double distance = calculateDistance(
                    targetLat, targetLng, 
                    pharmacy.getLatitude(), pharmacy.getLongitude()
                );
                return distance <= radiusKm;
            })
            .sorted((p1, p2) -> {
                double dist1 = calculateDistance(targetLat, targetLng, p1.getLatitude(), p1.getLongitude());
                double dist2 = calculateDistance(targetLat, targetLng, p2.getLatitude(), p2.getLongitude());
                return Double.compare(dist1, dist2);
            })
            .collect(Collectors.toList());
    }

    public List<PharmacyStore> findNearbyPharmacies(double latitude, double longitude, double radiusKm) {
        List<PharmacyStore> allPharmacies = pharmacyStoreRepository.findAll();
        
        return allPharmacies.stream()
            .filter(pharmacy -> pharmacy.getLatitude() != null && pharmacy.getLongitude() != null)
            .filter(pharmacy -> {
                double distance = calculateDistance(
                    latitude, longitude, 
                    pharmacy.getLatitude(), pharmacy.getLongitude()
                );
                return distance <= radiusKm;
            })
            .sorted((p1, p2) -> {
                double dist1 = calculateDistance(latitude, longitude, p1.getLatitude(), p1.getLongitude());
                double dist2 = calculateDistance(latitude, longitude, p2.getLatitude(), p2.getLongitude());
                return Double.compare(dist1, dist2);
            })
            .collect(Collectors.toList());
    }

    public PharmacyStore findNearestPharmacy(String pincode) {
        List<PharmacyStore> nearbyPharmacies = findNearbyPharmacies(pincode, 50.0); // 50km radius
        return nearbyPharmacies.isEmpty() ? null : nearbyPharmacies.get(0);
    }

    public PharmacyStore findNearestPharmacy(double latitude, double longitude) {
        List<PharmacyStore> nearbyPharmacies = findNearbyPharmacies(latitude, longitude, 50.0); // 50km radius
        return nearbyPharmacies.isEmpty() ? null : nearbyPharmacies.get(0);
    }

    public double getDistanceToPharmacy(String pincode, Long pharmacyId) {
        double[] coordinates = geocodingService.getCoordinates(pincode);
        double targetLat = coordinates[0];
        double targetLng = coordinates[1];

        PharmacyStore pharmacy = pharmacyStoreRepository.findById(pharmacyId).orElse(null);
        if (pharmacy == null || pharmacy.getLatitude() == null || pharmacy.getLongitude() == null) {
            return -1; // Invalid pharmacy or coordinates
        }

        return calculateDistance(targetLat, targetLng, pharmacy.getLatitude(), pharmacy.getLongitude());
    }

    public double getDistanceToPharmacy(double latitude, double longitude, Long pharmacyId) {
        PharmacyStore pharmacy = pharmacyStoreRepository.findById(pharmacyId).orElse(null);
        if (pharmacy == null || pharmacy.getLatitude() == null || pharmacy.getLongitude() == null) {
            return -1; // Invalid pharmacy or coordinates
        }

        return calculateDistance(latitude, longitude, pharmacy.getLatitude(), pharmacy.getLongitude());
    }

    /**
     * Calculate distance between two points using Haversine formula
     * @param lat1 Latitude of first point
     * @param lng1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lng2 Longitude of second point
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int EARTH_RADIUS = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }

    /**
     * Get delivery time estimate based on distance
     * @param distanceKm Distance in kilometers
     * @return Estimated delivery time in minutes
     */
    public int getEstimatedDeliveryTime(double distanceKm) {
        if (distanceKm <= 5) {
            return 30; // 30 minutes for nearby delivery
        } else if (distanceKm <= 10) {
            return 60; // 1 hour for medium distance
        } else if (distanceKm <= 20) {
            return 120; // 2 hours for far distance
        } else {
            return 240; // 4 hours for very far distance
        }
    }

    /**
     * Calculate delivery fee based on distance
     * @param distanceKm Distance in kilometers
     * @return Delivery fee in rupees
     */
    public double calculateDeliveryFee(double distanceKm) {
        double baseFee = 20.0; // Base delivery fee
        
        if (distanceKm <= 5) {
            return baseFee;
        } else if (distanceKm <= 10) {
            return baseFee + 10.0;
        } else if (distanceKm <= 20) {
            return baseFee + 20.0;
        } else {
            return baseFee + 30.0;
        }
    }
}
