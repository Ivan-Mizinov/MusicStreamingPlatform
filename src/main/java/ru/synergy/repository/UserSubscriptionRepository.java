package ru.synergy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.synergy.model.User;
import ru.synergy.model.UserSubscription;

import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findByUserAndIsActive(User user, boolean isActive);
}
