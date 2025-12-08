package ru.synergy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.synergy.model.Playlist;
import ru.synergy.model.Track;
import ru.synergy.model.User;
import ru.synergy.service.PlaylistService;
import ru.synergy.service.TrackReviewService;
import ru.synergy.service.TrackService;
import ru.synergy.service.UserService;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final TrackService trackService;
    private final PlaylistService playlistService;
    private final UserService userService;
    private final TrackReviewService trackReviewService;

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

        List<Playlist> userPlaylists = user != null ? playlistService.getUserPlaylists(user) : List.of();
        model.addAttribute("playlists", userPlaylists);
        model.addAttribute("users", userService.findAll());

        List<User> following = user != null ? user.getFollowing() : List.of();
        model.addAttribute("followingIds", following.stream().map(User::getId).toList());
        model.addAttribute("currentUserId", user != null ? user.getId() : null);
        model.addAttribute("followedPlaylists", playlistService.getPlaylistsOfUsers(following));

        model.addAttribute("reviewsByTrack", tracks.stream()
                .collect(Collectors.toMap(
                        Track::getId,
                        t -> Optional.ofNullable(trackReviewService.getReviews(t.getId()))
                                .orElse(Collections.emptyList())
                )));

        model.addAttribute("avgRatings", tracks.stream()
                .collect(Collectors.toMap(
                        Track::getId,
                        t -> Optional.ofNullable(trackReviewService.getAverageRating(t.getId()))
                                .orElse(0.0)
                )));

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
        User user = userService.findByUsername(username).orElse(null);
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
        model.addAttribute("users", userService.findAll());
        List<User> following = user.getFollowing();
        model.addAttribute("followingIds", following.stream().map(User::getId).toList());
        model.addAttribute("currentUserId", user.getId());
        model.addAttribute("followedPlaylists", playlistService.getPlaylistsOfUsers(following));
        model.addAttribute("reviewsByTrack", tracks.stream()
                .collect(java.util.stream.Collectors.toMap(Track::getId, t -> trackReviewService.getReviews(t.getId()))));
        model.addAttribute("avgRatings", tracks.stream()
                .collect(java.util.stream.Collectors.toMap(Track::getId, t -> trackReviewService.getAverageRating(t.getId()))));
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
        User user = userService.findByUsername(username).orElse(null);
        if (user == null) {
            model.addAttribute("error", "Пользователь не найден");
        }
        return user;
    }
}
