package com.healthcare.healthcare_platform.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleMapsService {

    @Value("${google.maps.api.key}")
    private String apiKey;

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
}