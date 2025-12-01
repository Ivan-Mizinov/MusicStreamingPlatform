package ru.synergy.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import ru.synergy.service.UserDetailServiceImpl;

@Controller
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailServiceImpl userDetailServiceImpl;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder, UserDetailServiceImpl userDetailServiceImpl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailServiceImpl = userDetailServiceImpl;
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
        user.setRole(Role.USER);

        userRepository.save(user);

        return "redirect:/login?success=true";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute UserDto request, Model model) {
        try {
            UserDetails userDetails = userDetailServiceImpl.loadUserByUsername(request.username());

            if (!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
                model.addAttribute("error", "Неверный логин или пароль");
                return "login";
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    userDetails.getPassword(),
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            return "redirect:/";
        } catch (UsernameNotFoundException e) {
            model.addAttribute("error", "Пользователь не найден");
            return "login";
        }
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
