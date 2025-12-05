package ru.synergy.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.synergy.model.Playlist;
import ru.synergy.model.User;
import ru.synergy.repository.UserRepository;
import ru.synergy.service.PlaylistService;

import java.security.Principal;

@Controller
@RequestMapping("/playlist")
public class PlaylistController {
    private final PlaylistService playlistService;
    private final UserRepository userRepository;

    public PlaylistController(PlaylistService playlistService,
                              UserRepository userRepository) {
        this.playlistService = playlistService;
        this.userRepository = userRepository;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("playlist", new Playlist());
        return "playlist/create";
    }

    @PostMapping("/create")
    public String createPlaylist(@Valid @ModelAttribute("playlist") Playlist playlist,
                                 BindingResult result,
                                 Model model,
                                 Principal principal) {
        if (principal == null) {
            model.addAttribute("error", "Пользователь не авторизован");
            return "redirect:/login";
        }
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            model.addAttribute("error", "Пользователь не найден");
        }
        if (result.hasErrors()) {
            return "playlist/create";
        }
        playlist.setUser(user);
        playlistService.save(playlist);

        return "redirect:/";
    }
}
