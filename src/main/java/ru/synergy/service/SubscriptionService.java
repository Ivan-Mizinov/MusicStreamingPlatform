package ru.synergy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.synergy.model.User;
import ru.synergy.model.UserSubscription;
import ru.synergy.repository.UserRepository;
import ru.synergy.repository.UserSubscriptionRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final UserSubscriptionRepository subRepo;
    private final UserRepository userRepo;

    @Transactional
    public UserSubscription subscribe(String username, int days) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        UserSubscription activeSub = subRepo.findByUserAndIsActive(user, true)
                .orElse(null);
        LocalDateTime newEndDate;

        if (activeSub != null) {
            newEndDate = activeSub.getEndDate().plusDays(days);
        } else {
            activeSub = new UserSubscription();
            activeSub.setUser(user);
            activeSub.setStartDate(LocalDateTime.now());
            activeSub.setActive(true);
            newEndDate = LocalDateTime.now().plusDays(days);
        }
        activeSub.setEndDate(newEndDate);

        String paymentId = "Mock_" + UUID.randomUUID();
        activeSub.getPaymentIds().add(paymentId);

        return subRepo.save(activeSub);
    }

}
