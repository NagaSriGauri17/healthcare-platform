package com.healthcare.healthcare_platform.service;

import com.google.maps.model.LatLng;
import com.healthcare.healthcare_platform.entity.Hospital;
import com.healthcare.healthcare_platform.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final GoogleMapsService googleMapsService;

    public Hospital registerHospital(String name, String address, String city,
                                     String phone, String email,
                                     Double latitude, Double longitude) {
        Hospital hospital = new Hospital();
        hospital.setName(name);
        hospital.setAddress(address);
        hospital.setCity(city);
        hospital.setPhone(phone);
        hospital.setEmail(email);
        hospital.setRating(0.0);
        hospital.setLatitude(latitude);
        hospital.setLongitude(longitude);
        return hospitalRepository.save(hospital);
    }

    public Hospital geocodeAndSave(Long hospitalId, String address) {
        Hospital hospital = hospitalRepository.findById(hospitalId).orElse(null);
        if (hospital != null) {
            LatLng location = googleMapsService.geocodeAddress(address);
            if (location != null) {
                hospital.setLatitude(location.lat);
                hospital.setLongitude(location.lng);
                hospital.setAddress(address);
                return hospitalRepository.save(hospital);
            }
        }
        return null;
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public Hospital getHospitalById(Long id) {
        return hospitalRepository.findById(id).orElse(null);
    }

    public List<Hospital> searchByCity(String city) {
        return hospitalRepository.findByCityIgnoreCase(city);
    }

    public List<Hospital> searchByName(String name) {
        return hospitalRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Hospital> findNearbyHospitals(Double lat, Double lng, Double radius) {
        return hospitalRepository.findNearbyHospitals(lat, lng, radius);
    }
}