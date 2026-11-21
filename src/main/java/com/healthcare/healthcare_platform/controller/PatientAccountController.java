package com.healthcare.healthcare_platform.controller;

import com.healthcare.healthcare_platform.entity.User;
import com.healthcare.healthcare_platform.repository.UserRepository;
import com.healthcare.healthcare_platform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Patient sign-up (after phone OTP) and profile editing.
 * Sign-up details: name, phone, emergency phone, address, age, optional email.
 */
@RestController
@RequestMapping("/api/patient-account")
@RequiredArgsConstructor
public class PatientAccountController {

    private static final String PLACEHOLDER_DOMAIN = "@placeholder.healthcare.app";

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private static String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? "" : v.toString().trim();
    }

    private static boolean isPhone(String s) {
        return s.matches("\\d{10}");
    }

    /** Returns an error message, or null when the details are valid. */
    private static String validate(String name, String emergencyPhone, String address, String ageText, String email) {
        if (name.length() < 2) return "Please enter your name";
        if (!isPhone(emergencyPhone)) return "Emergency phone number must be 10 digits";
        if (address.length() < 3) return "Please enter your address";
        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            return "Please enter a valid age";
        }
        if (age < 1 || age > 120) return "Please enter a valid age";
        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) return "Please enter a valid email";
        return null;
    }

    private ResponseEntity<?> bad(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    /** Verifies the phone OTP and fills in the patient's details. */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, Object> body) {
        String phone = str(body, "phone");
        String otp = str(body, "otp");
        String name = str(body, "name");
        String emergencyPhone = str(body, "emergencyPhone");
        String address = str(body, "address");
        String ageText = str(body, "age");
        String email = str(body, "email").toLowerCase();

        if (!isPhone(phone)) return bad("Phone number must be 10 digits");
        String error = validate(name, emergencyPhone, address, ageText, email);
        if (error != null) return bad(error);

        Optional<User> found = userRepository.findByPhone(phone);
        if (found.isEmpty() || found.get().getOtpCode() == null || !found.get().getOtpCode().trim().equals(otp)) {
            return bad("Invalid OTP");
        }
        User user = found.get();
        if (Boolean.TRUE.equals(user.getProfileComplete())
                || (user.getName() != null && !user.getName().startsWith("Patient_"))) {
            return bad("This phone number is already registered. Please login.");
        }

        if (!email.isEmpty()) {
            Optional<User> sameEmail = userRepository.findByEmail(email);
            if (sameEmail.isPresent() && !sameEmail.get().getId().equals(user.getId())) {
                return bad("This email is already used by another account");
            }
            user.setEmail(email);
        }
        user.setName(name);
        user.setEmergencyPhone(emergencyPhone);
        user.setAddress(address);
        user.setAge(Integer.parseInt(ageText));
        user.setProfileComplete(true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Account created. Please login.", "id", user.getId()));
    }

    /** Updates the logged-in patient's details. The phone number cannot be changed. */
    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable Long userId,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @RequestBody Map<String, Object> body) {
        Optional<User> found = userRepository.findById(userId);
        if (found.isEmpty()) return bad("Patient not found");
        User user = found.get();

        // The token belongs to the phone number that logged in; it must match this patient
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return bad("Please login again");
        String tokenPhone;
        try {
            tokenPhone = jwtUtil.extractEmail(authHeader.substring(7));
        } catch (Exception e) {
            return bad("Please login again");
        }
        if (tokenPhone == null || !tokenPhone.equals(user.getPhone())) return bad("Not allowed");

        String name = str(body, "name");
        String emergencyPhone = str(body, "emergencyPhone");
        String address = str(body, "address");
        String ageText = str(body, "age");
        String email = str(body, "email").toLowerCase();

        String error = validate(name, emergencyPhone, address, ageText, email);
        if (error != null) return bad(error);

        if (email.isEmpty()) {
            if (user.getEmail() == null || !user.getEmail().endsWith(PLACEHOLDER_DOMAIN)) {
                // keep the existing real email when the field is simply left empty
                email = user.getEmail() == null ? "" : user.getEmail();
            }
        } else {
            Optional<User> sameEmail = userRepository.findByEmail(email);
            if (sameEmail.isPresent() && !sameEmail.get().getId().equals(user.getId())) {
                return bad("This email is already used by another account");
            }
            user.setEmail(email);
        }

        user.setName(name);
        user.setEmergencyPhone(emergencyPhone);
        user.setAddress(address);
        user.setAge(Integer.parseInt(ageText));
        user.setProfileComplete(true);
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }
}
