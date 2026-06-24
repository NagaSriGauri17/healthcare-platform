package com.healthcare.healthcare_platform.service;

import com.healthcare.healthcare_platform.entity.User;
import com.healthcare.healthcare_platform.repository.UserRepository;
import com.healthcare.healthcare_platform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private final Map<String, String> otpStorage = new HashMap<>();

    public String register(String name, String email, String phone) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword("");
        user.setRole("PATIENT");
        userRepository.save(user);
        return "User registered successfully";
    }

    public String sendOtpByEmail(String email) {
        String otp = generateOtp();
        otpStorage.put(email, otp);
        System.out.println("OTP for " + email + " is: " + otp);
        return "OTP sent to email successfully";
    }

    public String sendOtpByPhone(String phone) {
        String otp = generateOtp();
        otpStorage.put(phone, otp);
        System.out.println("OTP for phone " + phone + " is: " + otp);
        return "OTP sent to phone successfully";
    }

    public String verifyOtpByEmail(String email, String otp) {
        return verifyOtp(email, otp);
    }

    public String verifyOtpByPhone(String phone, String otp) {
        return verifyOtp(phone, otp);
    }

    private String verifyOtp(String key, String otp) {
        String storedOtp = otpStorage.get(key);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStorage.remove(key);
            return jwtUtil.generateToken(key);
        }
        return "Invalid OTP";
    }

    private String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }
}