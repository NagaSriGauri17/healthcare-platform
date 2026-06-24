package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> request) {
        String result = authService.register(
                request.get("name"),
                request.get("email"),
                request.get("phone")
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/send-otp/email")
    public ResponseEntity<String> sendOtpByEmail(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.sendOtpByEmail(request.get("email")));
    }

    @PostMapping("/send-otp/phone")
    public ResponseEntity<String> sendOtpByPhone(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.sendOtpByPhone(request.get("phone")));
    }

    @PostMapping("/verify-otp/email")
    public ResponseEntity<String> verifyOtpByEmail(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.verifyOtpByEmail(
                request.get("email"),
                request.get("otp")
        ));
    }

    @PostMapping("/verify-otp/phone")
    public ResponseEntity<String> verifyOtpByPhone(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.verifyOtpByPhone(
                request.get("phone"),
                request.get("otp")
        ));
    }
}