package ru.synergy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.synergy.model.Role;
import ru.synergy.model.User;
import ru.synergy.service.UserService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public String showUsers(Model model, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            return "redirect:/";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", userService.findAll());
        return "users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/role")
    public String updateRole(@PathVariable Long id, @RequestParam Role role) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setRole(role);
        userService.save(user);
        return "redirect:/users";
    }
}
