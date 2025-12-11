package ru.synergy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.synergy.model.User;
import ru.synergy.model.UserSubscription;
import ru.synergy.repository.UserRepository;
import ru.synergy.repository.UserSubscriptionRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {
    @Mock
    private UserSubscriptionRepository subRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User user;
    private UserSubscription activeSub;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        activeSub = new UserSubscription();
        activeSub.setId(100L);
        activeSub.setUser(user);
        activeSub.setStartDate(LocalDateTime.now().minusDays(10));
        activeSub.setEndDate(LocalDateTime.now().plusDays(5));
        activeSub.setActive(true);
        activeSub.getPaymentIds().add("Mock_12345");
    }

    @Test
    void subscribe_shouldCreateNewSubscriptionWhenNoActiveExists() {
        when(userRepo.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(subRepo.findByUserAndIsActive(user, true)).thenReturn(Optional.empty());
        when(subRepo.save(any(UserSubscription.class))).thenAnswer(i -> i.getArgument(0));

        UserSubscription result = subscriptionService.subscribe("testUser", 30);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getStartDate()).isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS));
        assertThat(result.getEndDate()).isCloseTo(LocalDateTime.now().plusDays(30), within(2, ChronoUnit.SECONDS));
        assertThat(result.isActive()).isTrue();
        assertThat(result.getPaymentIds()).hasSize(1);
        assertThat(result.getPaymentIds().get(0)).startsWith("Mock_");

        verify(userRepo).findByUsername("testUser");
        verify(subRepo).findByUserAndIsActive(user, true);
        verify(subRepo).save(any(UserSubscription.class));
    }

    @Test
    void subscribe_shouldExtendActiveSubscriptionWhenExists() {
        LocalDateTime originalEndDate = activeSub.getEndDate();
        LocalDateTime expectedEndDate = originalEndDate.plusDays(10);

        when(userRepo.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(subRepo.findByUserAndIsActive(user, true)).thenReturn(Optional.of(activeSub));
        when(subRepo.save(any(UserSubscription.class))).thenAnswer(i -> i.getArgument(0));

        UserSubscription result = subscriptionService.subscribe("testUser", 10);

        assertThat(result.getEndDate()).isCloseTo(expectedEndDate, within(2, ChronoUnit.SECONDS));
        assertThat(result.isActive()).isTrue();
        assertThat(result.getStartDate()).isEqualTo(activeSub.getStartDate());
        assertThat(result.getPaymentIds()).hasSize(2);
        assertThat(result.getPaymentIds().get(1)).startsWith("Mock_");

        verify(userRepo).findByUsername("testUser");
        verify(subRepo).findByUserAndIsActive(user, true);
        verify(subRepo).save(any(UserSubscription.class));
    }

    @Test
    void subscribe_shouldThrowExceptionWhenUserNotFound() {
        when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                (IllegalArgumentException.class),
                () -> subscriptionService.subscribe("unknown", 30)
        );

        assertThat(exception.getMessage()).isEqualTo("Пользователь не найден");

        verify(userRepo).findByUsername("unknown");
        verify(subRepo, never()).findByUserAndIsActive(any(), anyBoolean());
        verify(subRepo, never()).save(any());
    }

    @Test
    void subscribe_shouldGenerateUniquePaymentIdEachTime() {
        when(userRepo.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(subRepo.findByUserAndIsActive(user, true)).thenReturn(Optional.empty());
        when(subRepo.save(any(UserSubscription.class))).thenAnswer(i -> i.getArgument(0));

        UserSubscription sub1 = subscriptionService.subscribe("testUser", 10);
        UserSubscription sub2 = subscriptionService.subscribe("testUser", 15);

        String paymentId1 = sub1.getPaymentIds().get(0);
        String paymentId2 = sub2.getPaymentIds().get(0);

        assertThat(paymentId1).isNotEqualTo(paymentId2);
        assertThat(paymentId1).startsWith("Mock_");
        assertThat(paymentId2).startsWith("Mock_");
    }
}
