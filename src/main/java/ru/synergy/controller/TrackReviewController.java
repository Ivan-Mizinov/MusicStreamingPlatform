package ru.synergy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.synergy.model.User;
import ru.synergy.service.TrackReviewService;
import ru.synergy.service.UserService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class TrackReviewController {
    private final TrackReviewService trackReviewService;
    private final UserService userService;

    @PostMapping("/tracks/{trackId}/review")
    public String addReview(@PathVariable Long trackId,
                            @RequestParam(required = false) Integer rating,
                            @RequestParam(required = false) String comment,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Авторизуйтесь, чтобы оставлять отзывы");
            return "redirect:/login";
        }
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Пользователь не найден");
            return "redirect:/";
        }
        try {
            trackReviewService.addReview(trackId, user, rating, comment);
            redirectAttributes.addFlashAttribute("success", "Отзыв сохранен");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/";
    }
}
