package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.HealthRecord;
import com.healthcare.healthcare_platform.repository.HealthRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final S3Service s3Service;

    public HealthRecord uploadRecord(MultipartFile file, Long patientId,
                                     Long appointmentId, String recordType,
                                     String uploadedBy) {
        String s3Key = s3Service.uploadFile(file, patientId);

        HealthRecord record = new HealthRecord();
        record.setPatientId(patientId);
        record.setAppointmentId(appointmentId);
        record.setRecordType(recordType);
        record.setFileName(file.getOriginalFilename());
        record.setS3Key(s3Key);
        record.setFileSize(file.getSize() + " bytes");
        record.setUploadedBy(uploadedBy);
        record.setUploadedAt(LocalDateTime.now());

        return healthRecordRepository.save(record);
    }

    public List<HealthRecord> getPatientRecords(Long patientId) {
        return healthRecordRepository.findByPatientIdOrderByUploadedAtDesc(patientId);
    }

    public List<HealthRecord> getAppointmentRecords(Long appointmentId) {
        return healthRecordRepository.findByAppointmentIdOrderByUploadedAtDesc(appointmentId);
    }

    public Map<String, String> getDownloadUrl(Long recordId) {
        HealthRecord record = healthRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        String downloadUrl = s3Service.generateDownloadUrl(record.getS3Key());

        Map<String, String> response = new HashMap<>();
        response.put("fileName", record.getFileName());
        response.put("recordType", record.getRecordType());
        response.put("downloadUrl", downloadUrl);
        response.put("expiresIn", "15 minutes");
        return response;
    }

    public void deleteRecord(Long recordId) {
        healthRecordRepository.deleteById(recordId);
    }
}