package ru.synergy.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ru.synergy.model.SubscriptionPlan;
import ru.synergy.model.User;
import ru.synergy.model.UserSubscription;
import ru.synergy.repository.SubscriptionPlanRepository;
import ru.synergy.service.SubscriptionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoBean
    private SubscriptionPlanRepository planRepository;

    private List<SubscriptionPlan> plans;

    @BeforeEach
    void setup() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        plans = Arrays.asList(
                new SubscriptionPlan(1L, "Basic", BigDecimal.valueOf(100), 30),
                new SubscriptionPlan(2L, "Premium", BigDecimal.valueOf(200), 60)
        );
    }

    @Test
    @WithMockUser(username = "testUser")
    void showSubscriptionPage_success() throws Exception {
        when(planRepository.findAll()).thenReturn(plans);

        mockMvc.perform(get("/subscription"))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attribute("username", "testUser"))
                .andExpect(model().attribute("plans", plans));
    }

    @Test
    @WithMockUser(username = "testUser")
    void subscribe_success() throws Exception {
        UserSubscription subscription = new UserSubscription();
        subscription.setId(100L);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusDays(30));
        subscription.setActive(true);

        when(subscriptionService.subscribe("testUser", 30)).thenReturn(subscription);

        when(planRepository.findAll()).thenReturn(plans);

        mockMvc.perform(post("/subscribe")
                        .param("days", "30")
                        .param("username", "testUser"))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attribute("success", "Подписка оформлена успешно!"))
                .andExpect(model().attribute("subscription", subscription));
    }

    @Test
    @WithMockUser(username = "testUser")
    void subscribe_failure() throws Exception {
        when(subscriptionService.subscribe("invalidUser", 30))
                .thenThrow(new IllegalArgumentException("Пользователь не найден"));

        when(planRepository.findAll()).thenReturn(plans);

        mockMvc.perform(post("/subscribe")
                        .param("days", "30")
                        .param("username", "invalidUser"))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attribute("error", "Пользователь не найден"));
    }

    @Test
    void showSubscriptionPage_unauthorized() throws Exception {
        mockMvc.perform(get("/subscription"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void subscribe_missingParams() throws Exception {
        when(planRepository.findAll()).thenReturn(plans);

        mockMvc.perform(post("/subscribe")
                        .param("username", "testUser"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/subscribe")
                        .param("days", "30"))
                .andExpect(status().isBadRequest());
    }
}
