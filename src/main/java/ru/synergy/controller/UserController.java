package ru.synergy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.synergy.model.User;
import ru.synergy.service.UserService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/users/{id}/follow")
    public String follow(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Авторизуйтесь, чтобы подписываться");
            return "redirect:/login";
        }
        User current = userService.findByUsername(principal.getName()).orElse(null);
        User target = userService.findById(id).orElse(null);
        if (current == null || target == null) {
            redirectAttributes.addFlashAttribute("error", "Пользователь не найден");
            return "redirect:/";
        }
        userService.follow(current, target);
        redirectAttributes.addFlashAttribute("success", "Подписка сохранена");
        return "redirect:/";
    }

    @PostMapping("/users/{id}/unfollow")
    public String unfollow(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Авторизуйтесь, чтобы отписываться");
            return "redirect:/login";
        }
        User current = userService.findByUsername(principal.getName()).orElse(null);
        User target = userService.findById(id).orElse(null);
        if (current == null || target == null) {
            redirectAttributes.addFlashAttribute("error", "Пользователь не найден");
            return "redirect:/";
        }
        userService.unfollow(current, target);
        redirectAttributes.addFlashAttribute("success", "Вы отписались");
        return "redirect:/";
    }
}
