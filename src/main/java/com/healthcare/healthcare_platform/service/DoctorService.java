package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.Doctor;
import com.healthcare.healthcare_platform.entity.Hospital;
import com.healthcare.healthcare_platform.entity.Specialty;
import com.healthcare.healthcare_platform.repository.DoctorRepository;
import com.healthcare.healthcare_platform.repository.HospitalRepository;
import com.healthcare.healthcare_platform.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final SpecialtyRepository specialtyRepository;

    public Doctor addDoctor(String name, String phone, String email,
                            Double fee, String experience,
                            Long hospitalId, Long specialtyId) {
        Hospital hospital = hospitalRepository.findById(hospitalId).orElse(null);
        Specialty specialty = specialtyRepository.findById(specialtyId).orElse(null);

        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setPhone(phone);
        doctor.setEmail(email);
        doctor.setConsultationFee(fee);
        doctor.setExperience(experience);
        doctor.setRating(0.0);
        doctor.setHospital(hospital);
        doctor.setSpecialty(specialty);
        return doctorRepository.save(doctor);
    }

    public List<Doctor> getDoctorsByHospital(Long hospitalId) {
        return doctorRepository.findByHospitalId(hospitalId);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id).orElse(null);
    }

    public List<Doctor> searchDoctors(String specialty, String city) {
        if (specialty != null && city != null) {
            return doctorRepository.findBySpecialtyAndCity(specialty, city);
        } else if (specialty != null) {
            return doctorRepository.findBySpecialty(specialty);
        }
        return doctorRepository.findAll();
    }
}