package com.example.demo.service;

import com.example.demo.model.DoctorVerificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoctorVerificationService {

    @Autowired
    private RestTemplate restTemplate;

    // NMC API endpoint for doctor verification
    private final String NMC_API_URL = "https://www.nmc.org.in/MCIRest/open/getPaginatedData";

    public boolean verifyDoctor(DoctorVerificationRequest request) {
        try {
            System.out.println("=== VERIFYING DOCTOR ===");
            System.out.println("Full Name: " + request.getFullName());
            System.out.println("Registration Number: " + request.getRegistrationNumber());
            System.out.println("User ID: " + request.getUserId());
            // Build the NMC API URL with query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(NMC_API_URL)
                    .queryParam("service", "getPaginatedDoctor")
                    .queryParam("registrationNo", request.getRegistrationNumber())
                    .queryParam("name", request.getFullName())
                    .queryParam("start", 0)
                    .queryParam("length", 1);

            String verificationUrl = builder.toUriString();
            System.out.println("Calling NMC API: " + verificationUrl);

            // Call the NMC API
            ResponseEntity<Map> response = restTemplate.getForEntity(verificationUrl, Map.class);

            System.out.println("NMC API Response Status: " + response.getStatusCode());
            System.out.println("NMC API Response Body: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<?> data = (List<?>) responseBody.get("data");

                if (data != null && !data.isEmpty()) {
                    System.out.println("Doctor verification successful! Found in NMC database.");
                    return true;
                } else {
                    System.out.println("Doctor verification failed! Not found in NMC database.");
                    return false;
                }
            }

            System.out.println("NMC API call failed or returned invalid response.");
            return false;

        } catch (Exception e) {
            System.err.println("Error verifying doctor with NMC API: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}