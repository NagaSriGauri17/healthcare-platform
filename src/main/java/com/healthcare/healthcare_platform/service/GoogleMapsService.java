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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class GoogleMapsService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // ───────── Live hospital discovery (city / hospital name), with paging done by the controller ─────────

    private static final String[] STATES = {"Andhra Pradesh", "Telangana", "Karnataka", "Tamil Nadu", "Maharashtra"};
    private static final String[] CITY_QUERIES = {
            "hospitals in %s", "multispecialty hospital in %s", "government hospital in %s",
            "private hospital in %s", "nursing home in %s", "children hospital in %s",
            "maternity hospital in %s", "eye hospital in %s", "super speciality hospital in %s"
    };
    private static final int MAX_RESULTS = 150;
    private static final long CACHE_MS = 6L * 60 * 60 * 1000;

    private static final ExecutorService POOL = Executors.newFixedThreadPool(6);
    private final Map<String, Object[]> searchCache = new ConcurrentHashMap<>(); // key -> {timestamp, list}

    /**
     * type "city": up to ~150 hospitals in that city (several searches, merged, no duplicates).
     * type "name": hospitals with that name in Andhra Pradesh, Telangana, Karnataka, Tamil Nadu and Maharashtra.
     * Results are cached for 6 hours so paging through them does not cost extra searches.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> discoverHospitals(String type, String query) {
        String q = query == null ? "" : query.trim();
        if (q.length() < 2) return new ArrayList<>();
        String key = type + "|" + q.toLowerCase();

        Object[] cached = searchCache.get(key);
        if (cached != null && System.currentTimeMillis() - (Long) cached[0] < CACHE_MS) {
            return (List<Map<String, Object>>) cached[1];
        }

        List<String> queries = new ArrayList<>();
        if ("name".equals(type)) {
            for (String state : STATES) queries.add(q + " hospital in " + state);
        } else {
            for (String template : CITY_QUERIES) queries.add(String.format(template, q));
        }

        List<CompletableFuture<List<Map<String, Object>>>> futures = new ArrayList<>();
        for (String textQuery : queries) {
            futures.add(CompletableFuture.supplyAsync(() -> runTextSearch(textQuery), POOL));
        }

        Map<String, Map<String, Object>> merged = new LinkedHashMap<>();
        for (CompletableFuture<List<Map<String, Object>>> f : futures) {
            try {
                for (Map<String, Object> place : f.get()) {
                    String id = String.valueOf(place.get("placeId"));
                    merged.putIfAbsent(id, place);
                }
            } catch (Exception e) {
                System.out.println("Discovery search error: " + e.getMessage());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        String[] tokens = q.toLowerCase().split("\\s+");
        for (Map<String, Object> place : merged.values()) {
            String text = (place.get("name") + " " + place.get("address")).toLowerCase();
            if ("name".equals(type)) {
                boolean inRegion = false;
                for (String state : STATES) {
                    if (text.contains(state.toLowerCase())) { inRegion = true; break; }
                }
                if (!inRegion) continue;
                boolean matches = true;
                for (String token : tokens) {
                    if (token.length() >= 3 && !text.contains(token)) { matches = false; break; }
                }
                if (!matches) continue;
            } else {
                place.put("city", q);
            }
            result.add(place);
        }

        // Well-rated hospitals with many reviews first
        result.sort((a, b) -> Double.compare(score(b), score(a)));
        if (result.size() > MAX_RESULTS) result = new ArrayList<>(result.subList(0, MAX_RESULTS));

        if (!result.isEmpty()) searchCache.put(key, new Object[]{System.currentTimeMillis(), result});
        return result;
    }

    private static double score(Map<String, Object> place) {
        double rating = place.get("rating") instanceof Number ? ((Number) place.get("rating")).doubleValue() : 0;
        double count = place.get("ratingCount") instanceof Number ? ((Number) place.get("ratingCount")).doubleValue() : 0;
        return rating * Math.log(1 + count);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> runTextSearch(String textQuery) {
        List<Map<String, Object>> out = new ArrayList<>();
        String pageToken = null;
        try {
            for (int page = 0; page < 3; page++) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Goog-Api-Key", apiKey);
                headers.set("X-Goog-FieldMask",
                        "places.displayName,places.formattedAddress,places.location,places.rating,places.userRatingCount,places.nationalPhoneNumber,places.id,nextPageToken");

                Map<String, Object> body = new HashMap<>();
                body.put("textQuery", textQuery);
                body.put("pageSize", 20);
                body.put("regionCode", "IN");
                if (pageToken != null) body.put("pageToken", pageToken);

                Map<String, Object> response = restTemplate.postForObject(
                        "https://places.googleapis.com/v1/places:searchText",
                        new HttpEntity<>(body, headers), Map.class);
                if (response == null || !response.containsKey("places")) break;

                for (Map<String, Object> place : (List<Map<String, Object>>) response.get("places")) {
                    Map<String, Object> h = new HashMap<>();
                    Map<String, Object> displayName = (Map<String, Object>) place.get("displayName");
                    h.put("name", displayName != null ? displayName.get("text") : "Unknown");
                    h.put("address", place.getOrDefault("formattedAddress", ""));
                    Map<String, Object> location = (Map<String, Object>) place.get("location");
                    if (location != null) {
                        h.put("latitude", location.get("latitude"));
                        h.put("longitude", location.get("longitude"));
                    }
                    h.put("rating", place.getOrDefault("rating", 0.0));
                    h.put("ratingCount", place.getOrDefault("userRatingCount", 0));
                    h.put("phone", place.getOrDefault("nationalPhoneNumber", ""));
                    h.put("placeId", place.getOrDefault("id", ""));
                    h.put("registered", false);
                    out.add(h);
                }
                Object next = response.get("nextPageToken");
                if (next == null) break;
                pageToken = next.toString();
            }
        } catch (Exception e) {
            System.out.println("Places search error for '" + textQuery + "': " + e.getMessage());
        }
        return out;
    }

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
        String pageToken = null;
        int pagesFetched = 0;
        int maxPages = 3; // up to 60 results (20 per page)

        try {
            while (pagesFetched < maxPages) {
                String url = "https://places.googleapis.com/v1/places:searchText";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Goog-Api-Key", apiKey);
                headers.set("X-Goog-FieldMask",
                        "places.displayName,places.formattedAddress,places.location,places.rating,places.id,nextPageToken");

                Map<String, Object> body = new HashMap<>();
                body.put("textQuery", "hospitals in " + city);
                body.put("pageSize", 20);
                if (pageToken != null) {
                    body.put("pageToken", pageToken);
                }

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

                if (response == null || !response.containsKey("places")) {
                    break;
                }

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

                    hospital.put("rating", place.getOrDefault("rating", 0.0));
                    hospital.put("placeId", place.getOrDefault("id", ""));

                    hospitalList.add(hospital);
                }

                pagesFetched++;
                System.out.println("=== Page " + pagesFetched + ": got " + places.size() + " places, response keys: " + response.keySet() + " ===");

                Object nextToken = response.get("nextPageToken");
                if (nextToken == null || places.isEmpty()) {
                    break; // no more pages
                }
                pageToken = nextToken.toString();

                // Google requires a short delay before a pageToken becomes valid
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            System.out.println("Places search error: " + e.getMessage());
        }
        return hospitalList;
    }
}