package ru.synergy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.synergy.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
