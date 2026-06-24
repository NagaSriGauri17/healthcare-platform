package com.healthcare.healthcare_platform.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service.account.path:firebase-service-account.json}")
    private String serviceAccountPath;

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount =
                    getClass().getClassLoader().getResourceAsStream(serviceAccountPath);

            if (serviceAccount != null && FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully");
            } else if (FirebaseApp.getApps().isEmpty()) {
                System.out.println("Firebase service account file not found — push notifications disabled");
            }
        } catch (IOException e) {
            System.out.println("Firebase initialization skipped: " + e.getMessage());
        }
    }
}