package ru.synergy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.synergy.model.SubscriptionPlan;

import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> findByName(String name);
}
