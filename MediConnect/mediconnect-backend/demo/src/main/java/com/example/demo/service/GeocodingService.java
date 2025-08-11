package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeocodingService {

    @Value("${geocoding.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public double[] getCoordinates(String address) {
        String url = "https://api.opencagedata.com/geocode/v1/json?q=" +
                URLEncoder.encode(address, StandardCharsets.UTF_8) +
                "&key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        try {
            JSONObject json = new JSONObject(response.getBody());
            JSONObject geometry = json.getJSONArray("results")
                    .getJSONObject(0)
                    .getJSONObject("geometry");

            double lat = geometry.getDouble("lat");
            double lng = geometry.getDouble("lng");

            return new double[]{lat, lng};
        } catch (Exception e) {
            throw new RuntimeException("Failed to get coordinates for address: " + address, e);
        }
    }


}
