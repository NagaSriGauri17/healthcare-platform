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

    // NEW — set password after OTP verified
    @PostMapping("/set-password")
    public ResponseEntity<String> setPassword(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.setPassword(
                request.get("identifier"),
                request.get("password")
        ));
    }

    // NEW — login with email/phone + password
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.login(
                request.get("identifier"),
                request.get("password")
        ));
    }

    // NEW — forgot password, send OTP to reset
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<String> forgotPasswordSendOtp(@RequestBody Map<String, String> request) {
        String identifier = request.get("identifier");
        if (identifier.contains("@")) {
            return ResponseEntity.ok(authService.sendOtpByEmail(identifier));
        } else {
            return ResponseEntity.ok(authService.sendOtpByPhone(identifier));
        }
    }

    // NEW — verify OTP then reset password
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<String> forgotPasswordReset(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.resetPassword(
                request.get("identifier"),
                request.get("otp"),
                request.get("newPassword")
        ));
    }
}