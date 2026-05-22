package com.labournow.backend.config;

import com.labournow.backend.entity.Category;
import com.labournow.backend.entity.User;
import com.labournow.backend.entity.WorkerProfile;
import com.labournow.backend.repository.CategoryRepository;
import com.labournow.backend.repository.UserRepository;
import com.labournow.backend.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;

    @Override
    public void run(String... args) throws Exception {
        // ── Seed Categories ──────────────────────────────────────────────────
        if (categoryRepository.count() == 0) {
            System.out.println("[DataSeeder] Seeding categories...");
            List<Category> categories = List.of(
                    Category.builder().name("Electricians").iconName("zap").build(),
                    Category.builder().name("Plumbers").iconName("droplet").build(),
                    Category.builder().name("Painters").iconName("paint-bucket").build(),
                    Category.builder().name("Carpenters").iconName("hammer").build(),
                    Category.builder().name("Masons").iconName("brick-wall").build(),
                    Category.builder().name("Helpers").iconName("users").build(),
                    Category.builder().name("Cleaners").iconName("sparkles").build(),
                    Category.builder().name("AC Technicians").iconName("wind").build()
            );
            categoryRepository.saveAll(categories);
        }

        // ── Seed Demo Workers (only if no workers exist) ─────────────────────
        if (workerProfileRepository.count() == 0) {
            System.out.println("[DataSeeder] Seeding demo workers...");

            seedWorker("Raju Kumar",      "9876500001", "123456789012", "Electricians",  450, 4.8);
            seedWorker("Suresh Verma",    "9876500002", "234567890123", "Plumbers",      400, 4.6);
            seedWorker("Amit Singh",      "9876500003", "345678901234", "Painters",      350, 4.9);
            seedWorker("Mahesh Yadav",    "9876500004", "456789012345", "Carpenters",    500, 4.7);
            seedWorker("Deepak Sharma",   "9876500005", "567890123456", "Electricians",  420, 4.5);
            seedWorker("Priya Devi",      "9876500006", "678901234567", "Cleaners",      300, 4.8);
            seedWorker("Rajesh Patel",    "9876500007", "789012345678", "AC Technicians",600, 4.9);
            seedWorker("Kiran Reddy",     "9876500008", "890123456789", "Helpers",       250, 4.3);

            System.out.println("[DataSeeder] Demo workers seeded successfully.");
        }

        // ── Seed Chefs Category specifically (for updates) ───────────────────
        boolean hasChefs = categoryRepository.findAll().stream().anyMatch(c -> c.getName().equals("Chefs"));
        if (!hasChefs) {
            System.out.println("[DataSeeder] Seeding Chefs category and workers...");
            categoryRepository.save(Category.builder().name("Chefs").iconName("utensils").build());
            seedWorker("Sanjeev Kapoor", "9876500009", "901234567890", "Chefs", 800, 4.9);
            seedWorker("Ranveer Brar",   "9876500010", "012345678901", "Chefs", 750, 4.8);
            System.out.println("[DataSeeder] Chefs seeded successfully.");
        }
    }

    private void seedWorker(String name, String phone, String aadhaar, String skill, int rate, double rating) {
        if (userRepository.findByPhone(phone).isPresent()) return; // skip if already exists

        User user = User.builder()
                .name(name)
                .phone(phone)
                .role(User.Role.WORKER)
                .isActive(true)
                .profilePictureUrl("https://i.pravatar.cc/150?u=" + phone)
                .build();
        user = userRepository.save(user);

        WorkerProfile profile = WorkerProfile.builder()
                .user(user)
                .aadhaarNumber(aadhaar)
                .primarySkill(skill)
                .isVerified(true)
                .isAvailable(true)
                .isOnline(true)
                .currentLatitude(12.971500 + (Math.random() * 0.05))
                .currentLongitude(77.594500 + (Math.random() * 0.05))
                .averageRating(BigDecimal.valueOf(rating))
                .hourlyRate(BigDecimal.valueOf(rate))
                .totalEarnings(BigDecimal.ZERO)
                .totalJobsCompleted((int)(Math.random() * 50) + 5)
                .build();
        workerProfileRepository.save(profile);
    }
}

