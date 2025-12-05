package ru.synergy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.synergy.model.Playlist;
import ru.synergy.model.Track;
import ru.synergy.model.User;
import ru.synergy.repository.UserRepository;
import ru.synergy.service.PlaylistService;
import ru.synergy.service.TrackService;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class MainController {
    private final TrackService trackService;
    private final PlaylistService playlistService;
    private final UserRepository userRepository;

    public MainController(TrackService trackService,
                          PlaylistService playlistService,
                          UserRepository userRepository) {
        this.trackService = trackService;
        this.playlistService = playlistService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String showMainPage(Model model,
                               @RequestParam(required = false) String search,
                               Principal principal) {
        User user = getCurrentUser(model, principal);

        List<Track> tracks;
        if (search != null && !search.trim().isEmpty()) {
            tracks = trackService.searchTracks(search.trim());
            model.addAttribute("searchQuery", search.trim());
        } else {
            tracks = trackService.getAllTracks();
        }

        model.addAttribute("tracks", tracks);

        List<Playlist> userPlaylists = playlistService.getUserPlaylists(user);
        model.addAttribute("playlists", userPlaylists);

        return "main";
    }

    @GetMapping("/playlist/{id}")
    public String showPlaylistTracks(
            @PathVariable Long id,
            Model model,
            Principal principal
    ) {
        if (principal == null) {
            model.addAttribute("error", "Пользователь не авторизован");
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            model.addAttribute("error", "Пользователь не найден");
            return "redirect:/";
        }

        Optional<Playlist> playlistOpt = playlistService.getPlaylistById(id);
        if (playlistOpt.isEmpty()) {
            model.addAttribute("error", "Плейлист не найден");
            return "redirect:/";
        }

        Playlist playlist = playlistOpt.get();
        List<Track> tracks = playlist.getTracks();

        model.addAttribute("tracks", tracks);
        model.addAttribute("playlists", playlistService.getUserPlaylists(user));
        model.addAttribute("activePlaylistId", id);

        model.addAttribute("playlistName", playlist.getName());

        return "main";
    }

    private User getCurrentUser(Model model, Principal principal) {
        if (principal == null) {
            model.addAttribute("error", "Пользователь не авторизован");
            return null;
        }
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            model.addAttribute("error", "Пользователь не найден");
        }
        return user;
    }
}
