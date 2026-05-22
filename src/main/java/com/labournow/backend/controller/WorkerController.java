package com.labournow.backend.controller;

import com.labournow.backend.entity.User;
import com.labournow.backend.entity.WorkerProfile;
import com.labournow.backend.repository.UserRepository;
import com.labournow.backend.repository.WorkerProfileRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;

    @Data
    public static class OnboardRequest {
        private String name;
        private String phone;
        private String aadhaarNumber;
        private String primarySkill;
        private String profilePictureUrl;
        private String hourlyRate;
    }

    @PostMapping("/onboard")
    @Transactional
    public ResponseEntity<?> onboardWorker(@RequestBody OnboardRequest request) {
        // Check if phone already registered
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phone number already registered. Please login."));
        }

        User user = User.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .role(User.Role.WORKER)
                .isActive(true)
                .profilePictureUrl(request.getProfilePictureUrl() != null ? request.getProfilePictureUrl() : "https://i.pravatar.cc/150?u=" + request.getPhone())
                .build();
        user = userRepository.save(user);

        BigDecimal rate = new BigDecimal("300.00");
        try {
            if (request.getHourlyRate() != null) rate = new BigDecimal(request.getHourlyRate());
        } catch (Exception ignored) {}

        WorkerProfile profile = WorkerProfile.builder()
                .user(user)
                .aadhaarNumber(request.getAadhaarNumber())
                .primarySkill(request.getPrimarySkill())
                .isVerified(true)
                .isAvailable(true)
                .isOnline(true)
                .currentLatitude(12.971500)
                .currentLongitude(77.594500)
                .averageRating(new BigDecimal("5.0"))
                .hourlyRate(rate)
                .totalEarnings(BigDecimal.ZERO)
                .build();
        workerProfileRepository.save(profile);

        return ResponseEntity.ok(Map.of(
            "message", "Onboarded successfully",
            "userId", user.getId(),
            "phone", user.getPhone(),
            "name", user.getName()
        ));
    }

    // Self-registration endpoint (worker enrolls themselves)
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerSelf(@RequestBody OnboardRequest request) {
        return onboardWorker(request);
    }

    @GetMapping
    public ResponseEntity<?> getAllWorkers(@RequestParam(required = false) String skill) {
        var stream = workerProfileRepository.findAll().stream();
        if (skill != null && !skill.isBlank()) {
            stream = stream.filter(wp -> skill.equalsIgnoreCase(wp.getPrimarySkill()));
        }
        return ResponseEntity.ok(stream.map(wp -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", wp.getUser().getId());         // userId — used for booking.workerId
            m.put("profileId", wp.getId());             // workerProfile.id — used for rate/toggle
            m.put("name", wp.getUser().getName());
            m.put("phone", wp.getUser().getPhone());
            m.put("profilePictureUrl", wp.getUser().getProfilePictureUrl() != null
                    ? wp.getUser().getProfilePictureUrl() : "https://i.pravatar.cc/150?u=" + wp.getUser().getPhone());
            m.put("primarySkill", wp.getPrimarySkill());
            m.put("status", wp.isVerified() ? "Active" : "Pending");
            m.put("averageRating", wp.getAverageRating() != null ? wp.getAverageRating().toString() : "5.0");
            m.put("hourlyRate", wp.getHourlyRate() != null ? wp.getHourlyRate().toString() : "300");
            m.put("isOnline", wp.isOnline());
            m.put("totalJobsCompleted", wp.getTotalJobsCompleted() != null ? wp.getTotalJobsCompleted() : 0);
            m.put("totalEarnings", wp.getTotalEarnings() != null ? wp.getTotalEarnings().toString() : "0");
            m.put("aadhaarNumber", wp.getAadhaarNumber());
            return m;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        return workerProfileRepository.findByUser(user)
            .map(wp -> ResponseEntity.ok(Map.of(
                "id", wp.getId(),
                "name", user.getName(),
                "phone", user.getPhone(),
                "profilePictureUrl", user.getProfilePictureUrl() != null ? user.getProfilePictureUrl() : "",
                "primarySkill", wp.getPrimarySkill(),
                "hourlyRate", wp.getHourlyRate() != null ? wp.getHourlyRate().toString() : "300",
                "isOnline", wp.isOnline(),
                "averageRating", wp.getAverageRating().toString(),
                "totalJobsCompleted", wp.getTotalJobsCompleted(),
                "totalEarnings", wp.getTotalEarnings() != null ? wp.getTotalEarnings().toString() : "0"
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class UpdateRateRequest {
        private String hourlyRate;
    }

    @PatchMapping("/{id}/rate")
    @Transactional
    public ResponseEntity<?> updateRate(@PathVariable Long id, @RequestBody UpdateRateRequest req) {
        return workerProfileRepository.findById(id).map(wp -> {
            try {
                wp.setHourlyRate(new BigDecimal(req.getHourlyRate()));
                workerProfileRepository.save(wp);
                return ResponseEntity.ok(Map.of("message", "Rate updated", "hourlyRate", req.getHourlyRate()));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid rate"));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/toggle-online")
    @Transactional
    public ResponseEntity<?> toggleOnline(@PathVariable Long id) {
        return workerProfileRepository.findById(id).map(wp -> {
            wp.setOnline(!wp.isOnline());
            wp.setAvailable(wp.isOnline());
            workerProfileRepository.save(wp);
            return ResponseEntity.ok(Map.of("isOnline", wp.isOnline()));
        }).orElse(ResponseEntity.notFound().build());
    }
}
