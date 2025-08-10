package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeocodingService {

    @Value("${geocoding.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Double> getCoordinates(String address) {
        if (address == null || address.trim().isEmpty()) {
            System.err.println("Address is null or empty");
            return null;
        }

        try {
            String encodedAddress = java.net.URLEncoder.encode(address.trim(), "UTF-8");
            String url = String.format(
                    "https://api.opencagedata.com/geocode/v1/json?q=%s&key=%s&limit=1",
                    encodedAddress, apiKey
            );

            System.out.println("Calling OpenCageData API for address: " + address);
            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                System.err.println("No response from OpenCageData API");
                return null;
            }

            JsonNode root = objectMapper.readTree(response);

            if (root != null && root.has("results") && root.get("results").size() > 0) {
                JsonNode result = root.get("results").get(0);
                JsonNode geometry = result.get("geometry");

                double lat = geometry.get("lat").asDouble();
                double lng = geometry.get("lng").asDouble();

                Map<String, Double> coordinates = new HashMap<>();
                coordinates.put("latitude", lat);
                coordinates.put("longitude", lng);

                System.out.println("Successfully got coordinates: " + lat + ", " + lng);
                return coordinates;
            } else {
                System.err.println("No results found for address: " + address);
                if (root != null && root.has("status")) {
                    JsonNode status = root.get("status");
                    System.err.println("API Status: " + status.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting coordinates for address: " + address);
            e.printStackTrace();
        }

        // Return null if geocoding fails
        return null;
    }

    public Map<String, Map<String, Double>> getCoordinatesBatch(List<String> addresses) {
        Map<String, Map<String, Double>> results = new HashMap<>();

        for (String address : addresses) {
            Map<String, Double> coordinates = getCoordinates(address);
            results.put(address, coordinates);

            // Add a small delay to avoid hitting API rate limits
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return results;
    }
}