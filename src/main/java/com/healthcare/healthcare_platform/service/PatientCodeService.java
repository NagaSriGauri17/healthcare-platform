package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.Appointment;
import com.healthcare.healthcare_platform.entity.Hospital;
import com.healthcare.healthcare_platform.repository.AppointmentRepository;
import com.healthcare.healthcare_platform.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientCodeService {

    private final AppointmentRepository appointmentRepository;
    private final HospitalRepository hospitalRepository;

    public String getHospitalPrefix(Hospital hospital) {
        String name = hospital.getName().toLowerCase();
        if (name.contains("apollo")) return "AP";
        if (name.contains("kamineni")) return "KM";
        if (name.contains("manipal")) return "MP";
        if (name.contains("sunrise")) return "SR";
        if (name.contains("andhra")) return "AH";
        String cleaned = hospital.getName().replaceAll("[^A-Za-z]", "");
        return cleaned.length() >= 2 ? cleaned.substring(0, 2).toUpperCase() : cleaned.toUpperCase();
    }

    public String getPatientCode(Long hospitalId, Long userId) {
        Hospital hospital = hospitalRepository.findById(hospitalId).orElse(null);
        if (hospital == null) return "P" + userId;

        List<Appointment> hospitalAppointments = appointmentRepository.findByHospitalIdOrderByCreatedAtAsc(hospitalId);
        List<Long> seenUserIds = new ArrayList<>();
        for (Appointment apt : hospitalAppointments) {
            if (apt.getUser() != null && !seenUserIds.contains(apt.getUser().getId())) {
                seenUserIds.add(apt.getUser().getId());
            }
        }
        int index = seenUserIds.indexOf(userId);
        int seq = index >= 0 ? index + 1 : seenUserIds.size() + 1;
        return getHospitalPrefix(hospital) + seq;
    }
}