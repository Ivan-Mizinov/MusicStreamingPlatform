package ru.synergy.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.synergy.model.Playlist;
import ru.synergy.model.Track;
import ru.synergy.model.User;
import ru.synergy.repository.UserRepository;
import ru.synergy.service.PlaylistService;
import ru.synergy.service.TrackService;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/playlist")
public class PlaylistController {
    private final PlaylistService playlistService;
    private final UserRepository userRepository;
    private final TrackService trackService;

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

    @PostMapping("/add-track")
    public String addTrackToPlaylist(
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam("playlistId") Long playlistId,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Пользователь не авторизован");
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Пользователь не найден");
            return "redirect:/";
        }

        Optional<Playlist> playlistOpt = playlistService.getPlaylistById(playlistId);

        if (playlistOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Плейлист не найден");
            return "redirect:/";
        }

        Optional<Track> trackOpt = trackService.getTrackByFileUrl(fileUrl);

        if (trackOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Трек не найден по URL: " + fileUrl);
            return "redirect:/";
        }

        Track track = trackOpt.get();
        Playlist playlist = playlistOpt.get();

        if (!playlist.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому плейлисту");
            return "redirect:/";
        }

        playlist.getTracks().add(track);
        playlistService.save(playlist);

        redirectAttributes.addFlashAttribute("success", "Трек добавлен в плейлист!");
        return "redirect:/";
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id) {
        try {
            playlistService.deletePlaylist(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Ошибка удаления плейлиста: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
