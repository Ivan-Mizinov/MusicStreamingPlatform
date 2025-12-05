package ru.synergy.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.synergy.model.Role;
import ru.synergy.model.User;
import ru.synergy.repository.UserRepository;

@Configuration
public class DataInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
        };
    }
}

