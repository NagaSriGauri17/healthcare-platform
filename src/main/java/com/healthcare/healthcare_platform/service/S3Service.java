package com.healthcare.healthcare_platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    @Value("${aws.access.key}")
    private String accessKey;

    @Value("${aws.secret.key}")
    private String secretKey;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    private S3Client s3Client;
    private S3Presigner presigner;

    @PostConstruct
    public void init() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credProvider = StaticCredentialsProvider.create(credentials);

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credProvider)
                .build();

        this.presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credProvider)
                .build();

        System.out.println("AWS S3 initialized successfully");
    }

    public String uploadFile(MultipartFile file, Long patientId) {
        try {
            String s3Key = "records/" + patientId + "/" +
                    UUID.randomUUID() + "-" + file.getOriginalFilename();

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
            System.out.println("File uploaded to S3: " + s3Key);
            return s3Key;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to S3: " + e.getMessage());
        }
    }

    public String generateDownloadUrl(String s3Key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(getRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }
}