package ru.synergy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.synergy.model.Role;
import ru.synergy.model.SubscriptionPlan;
import ru.synergy.model.User;
import ru.synergy.repository.SubscriptionPlanRepository;
import ru.synergy.repository.UserRepository;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionPlanRepository subRepo;

    @Bean
    CommandLineRunner initUsers() {
        return args -> {
            if (userRepository.findByUsername("administrator").isEmpty()) {
                User admin = new User();
                admin.setUsername("administrator");
                admin.setPasswordHash(passwordEncoder.encode("password1"));
                admin.setRole(Role.ROLE_ADMIN);
                userRepository.save(admin);
            }

            if (userRepository.findByUsername("FirstUser").isEmpty()) {
                User user = new User();
                user.setUsername("FirstUser");
                user.setPasswordHash(passwordEncoder.encode("password1"));
                user.setRole(Role.ROLE_USER);
                userRepository.save(user);
            }

            if (subRepo.findByName("Базовый").isEmpty()) {
                SubscriptionPlan basicPlan = new SubscriptionPlan();
                basicPlan.setName("Базовый");
                basicPlan.setPrice(new BigDecimal("299.00"));
                basicPlan.setDurationInDays(30);
                subRepo.save(basicPlan);
            }

            if (subRepo.findByName("Премиум").isEmpty()) {
                SubscriptionPlan basicPlan = new SubscriptionPlan();
                basicPlan.setName("Премиум");
                basicPlan.setPrice(new BigDecimal("799.00"));
                basicPlan.setDurationInDays(90);
                subRepo.save(basicPlan);
            }
        };
    }
}

