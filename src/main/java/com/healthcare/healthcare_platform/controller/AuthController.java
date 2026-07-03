package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.service.AuthService;
import com.healthcare.healthcare_platform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

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

    // set password after OTP verified — now accepts optional hospitalId for staff
    @PostMapping("/set-password")
    public ResponseEntity<String> setPassword(@RequestBody Map<String, String> request) {
        String hospitalIdStr = request.get("hospitalId");
        Long hospitalId = (hospitalIdStr != null && !hospitalIdStr.isEmpty())
                ? Long.valueOf(hospitalIdStr) : null;
        return ResponseEntity.ok(authService.setPassword(
                request.get("identifier"),
                request.get("password"),
                hospitalId
        ));
    }

    // login with email/phone + password
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.login(
                request.get("identifier"),
                request.get("password")
        ));
    }

    // forgot password, send OTP to reset
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<String> forgotPasswordSendOtp(@RequestBody Map<String, String> request) {
        String identifier = request.get("identifier");
        if (identifier.contains("@")) {
            return ResponseEntity.ok(authService.sendOtpByEmail(identifier));
        } else {
            return ResponseEntity.ok(authService.sendOtpByPhone(identifier));
        }
    }

    // verify OTP then reset password
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<String> forgotPasswordReset(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.resetPassword(
                request.get("identifier"),
                request.get("otp"),
                request.get("newPassword")
        ));
    }

    // NEW — returns logged-in user's id, name, role, hospitalId
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String identifier = jwtUtil.extractEmail(token);
        return ResponseEntity.ok(authService.getCurrentUser(identifier));
    }
}