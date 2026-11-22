package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.Appointment;
import com.healthcare.healthcare_platform.entity.Doctor;
import com.healthcare.healthcare_platform.entity.Hospital;
import com.healthcare.healthcare_platform.service.AppointmentService;
import com.healthcare.healthcare_platform.service.DoctorService;
import com.healthcare.healthcare_platform.service.GoogleMapsService;
import com.healthcare.healthcare_platform.service.HospitalService;
import com.healthcare.healthcare_platform.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;
    private final DoctorService doctorService;
    private final GoogleMapsService googleMapsService;
    private final AppointmentService appointmentService;
    private final QueueService queueService;

    @PostMapping("/register")
    public ResponseEntity<Hospital> register(@RequestBody Map<String, Object> request) {
        Double latitude = request.get("latitude") != null ?
                Double.valueOf(request.get("latitude").toString()) : null;
        Double longitude = request.get("longitude") != null ?
                Double.valueOf(request.get("longitude").toString()) : null;
        Hospital hospital = hospitalService.registerHospital(
                (String) request.get("name"),
                (String) request.get("address"),
                (String) request.get("city"),
                (String) request.get("phone"),
                (String) request.get("email"),
                latitude,
                longitude
        );
        return ResponseEntity.ok(hospital);
    }

    @PostMapping("/geocode/{id}")
    public ResponseEntity<Hospital> geocode(@PathVariable Long id,
                                            @RequestBody Map<String, String> request) {
        Hospital hospital = hospitalService.geocodeAndSave(id, request.get("address"));
        return ResponseEntity.ok(hospital);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Hospital>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    @GetMapping("/{id}/doctors")
    public ResponseEntity<?> getDoctorsByHospital(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorsByHospital(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Hospital>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name) {
        if (city != null) {
            return ResponseEntity.ok(hospitalService.searchByCity(city));
        } else if (name != null) {
            return ResponseEntity.ok(hospitalService.searchByName(name));
        }
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Hospital>> nearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam Double radius) {
        return ResponseEntity.ok(hospitalService.findNearbyHospitals(lat, lng, radius));
    }

    /**
     * Search used by the patient app, 10 results per page.
     * type=city: hospitals in that city (up to ~150). type=name: hospitals with that name in
     * Andhra Pradesh, Telangana, Karnataka, Tamil Nadu and Maharashtra.
     * Hospitals registered on ArogyaCare (bookable) always come first.
     */
    @GetMapping("/discover")
    public ResponseEntity<Map<String, Object>> discover(
            @RequestParam String q,
            @RequestParam(defaultValue = "city") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        size = Math.max(1, Math.min(size, 20));
        page = Math.max(0, page);
        String query = q.trim();
        boolean byName = "name".equalsIgnoreCase(type);

        List<Map<String, Object>> combined = new ArrayList<>();
        java.util.Set<String> seenNames = new java.util.HashSet<>();

        List<Hospital> registered = byName
                ? hospitalService.searchByName(query)
                : hospitalService.searchByCity(query);
        for (Hospital h : registered) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", h.getId());
            m.put("name", h.getName());
            m.put("address", h.getAddress());
            m.put("city", h.getCity());
            m.put("phone", h.getPhone());
            m.put("rating", h.getRating());
            m.put("latitude", h.getLatitude());
            m.put("longitude", h.getLongitude());
            m.put("registered", true);
            combined.add(m);
            seenNames.add(String.valueOf(h.getName()).toLowerCase().replaceAll("[^a-z0-9]", ""));
        }
        int registeredCount = combined.size();

        for (Map<String, Object> place : googleMapsService.discoverHospitals(byName ? "name" : "city", query)) {
            String norm = String.valueOf(place.get("name")).toLowerCase().replaceAll("[^a-z0-9]", "");
            if (seenNames.contains(norm)) continue;
            combined.add(place);
        }

        int total = combined.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);

        Map<String, Object> response = new HashMap<>();
        response.put("content", combined.subList(from, to));
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", total);
        response.put("totalPages", (int) Math.ceil(total / (double) size));
        response.put("registeredCount", registeredCount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/find-by-city")
    public ResponseEntity<List<Map<String, Object>>> liveSearch(
            @RequestParam String city) {
        return ResponseEntity.ok(googleMapsService.searchHospitalsByCity(city));
    }

    // Hospital-wide: today's appointments across ALL doctors in this hospital
    @GetMapping("/{hospitalId}/appointments/today")
    public ResponseEntity<List<Appointment>> getHospitalAppointmentsToday(@PathVariable Long hospitalId) {
        List<Doctor> doctors = doctorService.getDoctorsByHospital(hospitalId);
        List<Appointment> allAppointments = new ArrayList<>();
        for (Doctor doctor : doctors) {
            allAppointments.addAll(appointmentService.getDoctorAppointments(doctor.getId()));
        }
        return ResponseEntity.ok(allAppointments);
    }

    @GetMapping("/debug/env")
    public ResponseEntity<Map<String, Object>> debugEnv() {
        Map<String, Object> debug = new HashMap<>();
        debug.put("REDIS_URL_raw", System.getenv("REDIS_URL"));
        debug.put("SPRING_DATASOURCE_URL_raw", System.getenv("SPRING_DATASOURCE_URL"));
        debug.put("AWS_ACCESS_KEY_raw", System.getenv("AWS_ACCESS_KEY"));
        return ResponseEntity.ok(debug);
    }

    // Hospital-wide: queue summary across ALL doctors in this hospital
    @GetMapping("/{hospitalId}/queue/summary")
    public ResponseEntity<Map<String, Object>> getHospitalQueueSummary(@PathVariable Long hospitalId) {
        Map<String, Object> summary = new HashMap<>();
        try {
            List<Doctor> doctors = doctorService.getDoctorsByHospital(hospitalId);
            int totalCurrentToken = 0;
            int totalWaiting = 0;
            int totalCompleted = 0;
            for (Doctor doctor : doctors) {
                Map<String, Object> status = queueService.getQueueStatus(doctor.getId());
                Object current = status.get("currentToken");
                Object waiting = status.get("waitingCount");
                if (current != null) totalCurrentToken += Integer.parseInt(current.toString());
                if (waiting != null) totalWaiting += Integer.parseInt(waiting.toString());

                Map<String, Object> board = queueService.getQueueBoard(doctor.getId());
                List<?> completedList = (List<?>) board.get("completed");
                if (completedList != null) totalCompleted += completedList.size();
            }
            summary.put("currentToken", totalCurrentToken);
            summary.put("waitingCount", totalWaiting);
            summary.put("completedCount", totalCompleted);
            summary.put("doctorCount", doctors.size());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            Throwable rootCause = e;
            StringBuilder causeChain = new StringBuilder();
            while (rootCause != null) {
                causeChain.append(rootCause.getClass().getName())
                        .append(": ")
                        .append(rootCause.getMessage())
                        .append(" | ");
                rootCause = rootCause.getCause();
            }
            summary.put("error", e.toString());
            summary.put("causeChain", causeChain.toString());
            return ResponseEntity.status(500).body(summary);
        }
    }
}