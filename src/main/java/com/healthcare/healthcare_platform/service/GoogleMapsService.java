package com.healthcare.healthcare_platform.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlacesApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Map<String, Object>> searchHospitalsByCity(String city) {
        List<Map<String, Object>> hospitalList = new ArrayList<>();
        try {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build();

            LatLng cityLocation = geocodeAddress(city);
            if (cityLocation == null) {
                return hospitalList;
            }

            PlacesSearchResponse response = PlacesApi.textSearchQuery(context,
                    "hospitals in " + city).await();

            if (response.results != null) {
                for (PlacesSearchResult result : response.results) {
                    Map<String, Object> hospital = new HashMap<>();
                    hospital.put("name", result.name);
                    hospital.put("address", result.formattedAddress);
                    hospital.put("city", city);
                    hospital.put("latitude", result.geometry.location.lat);
                    hospital.put("longitude", result.geometry.location.lng);
                    hospital.put("rating", result.rating != null ? result.rating : 0.0);
                    hospital.put("placeId", result.placeId);
                    hospitalList.add(hospital);
                }
            }
        } catch (Exception e) {
            System.out.println("Places search error: " + e.getMessage());
        }
        return hospitalList;
    }
}