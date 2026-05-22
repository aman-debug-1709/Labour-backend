package com.labournow.backend.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String phone;
    private String otp; // In MVP, we mock OTP validation
    private String name; // Required only for registration
    private String role; // CUSTOMER or WORKER
}
