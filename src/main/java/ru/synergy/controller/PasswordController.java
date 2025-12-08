package ru.synergy.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.synergy.model.User;
import ru.synergy.repository.UserRepository;

@Controller
@RequestMapping("/password")
public class PasswordController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordController(UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
}
