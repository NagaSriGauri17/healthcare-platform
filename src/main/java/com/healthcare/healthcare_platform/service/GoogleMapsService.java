package com.healthcare.healthcare_platform.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleMapsService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public LatLng geocodeAddress(String address) {
        try {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build();
            GeocodingResult[] results = GeocodingApi
                    .geocode(context, address).await();
            if (results != null && results.length > 0) {
                return results[0].geometry.location;
            }
        } catch (Exception e) {
            System.out.println("Geocoding error: " + e.getMessage());
        }
        return null;
    }

    public List<Map<String, Object>> searchHospitalsByCity(String city) {
        List<Map<String, Object>> hospitalList = new ArrayList<>();
        try {
            String url = "https://places.googleapis.com/v1/places:searchText";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Goog-Api-Key", apiKey);
            headers.set("X-Goog-FieldMask",
                    "places.displayName,places.formattedAddress,places.location,places.rating,places.id");

            Map<String, Object> body = new HashMap<>();
            body.put("textQuery", "hospitals in " + city);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && response.containsKey("places")) {
                List<Map<String, Object>> places = (List<Map<String, Object>>) response.get("places");
                for (Map<String, Object> place : places) {
                    Map<String, Object> hospital = new HashMap<>();

                    Map<String, Object> displayName = (Map<String, Object>) place.get("displayName");
                    hospital.put("name", displayName != null ? displayName.get("text") : "Unknown");

                    hospital.put("address", place.getOrDefault("formattedAddress", ""));
                    hospital.put("city", city);

                    Map<String, Object> location = (Map<String, Object>) place.get("location");
                    if (location != null) {
                        hospital.put("latitude", location.get("latitude"));
                        hospital.put("longitude", location.get("longitude"));
                    }

                    hospital.put("rating", result.rating > 0 ? (double) result.rating : 0.0);
                    hospital.put("placeId", place.getOrDefault("id", ""));

                    hospitalList.add(hospital);
                }
            }
        } catch (Exception e) {
            System.out.println("Places search error: " + e.getMessage());
        }
        return hospitalList;
    }
}