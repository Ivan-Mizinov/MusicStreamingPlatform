package ru.synergy.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.synergy.dto.UserDto;
import ru.synergy.model.Role;
import ru.synergy.model.User;
import ru.synergy.repository.UserRepository;

@Controller
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String register(@ModelAttribute UserDto request, Model model) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            model.addAttribute("error", "Пользователь с таким логином уже существует");
            return "register";
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.ROLE_USER);

        userRepository.save(user);

        return "redirect:/login?success=true";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("error", null);
        return "register";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("error", null);
        return "login";
    }
}
