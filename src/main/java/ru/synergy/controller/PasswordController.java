package ru.synergy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.synergy.model.User;
import ru.synergy.repository.UserRepository;

import java.util.Collections;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/password")
public class PasswordController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/change")
    public String showChangePasswordForm(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("username", username);
        return "change-password";
    }

    @PostMapping("/change")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmNewPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            model.addAttribute("error", "Текущий пароль не верный");
            return "change-password";
        }

        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
            model.addAttribute("error",
                    "Пароль должен содержать минимум 8 символов, включая буквы и цифры");
            return "change-password";
        }

        if (!newPassword.equals(confirmNewPassword)) {
            model.addAttribute("error", "Новый пароль и подтверждение не совпадают");
            return "change-password";
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Пароль успешно изменён");
        return "redirect:/";

    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUsernameNotFound(
            UsernameNotFoundException ex) {
        Map<String, String> body = Collections.singletonMap("error", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }
}
