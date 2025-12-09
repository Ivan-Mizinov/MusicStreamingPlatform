package ru.synergy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.synergy.model.UserSubscription;
import ru.synergy.service.SubscriptionService;
import ru.synergy.repository.SubscriptionPlanRepository;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionPlanRepository planRepository;

    @GetMapping("/subscription")
    public String showSubscriptionPage(Model model, Principal principal) {
        String username = principal.getName();
        model.addAttribute("username", username);
        model.addAttribute("plans", planRepository.findAll());
        return "subscription";
    }

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam("days") int days,
                            @RequestParam("username") String username,
                            Model model) {
        try {
            UserSubscription subscription = subscriptionService.subscribe(username, days);
            model.addAttribute("success", "Подписка оформлена успешно!");
            model.addAttribute("subscription", subscription);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "subscription";
    }
}
