package ru.synergy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.synergy.model.Track;
import ru.synergy.service.PlaylistService;
import ru.synergy.service.TrackService;

import java.util.List;

@Controller
public class MainController {
    private final TrackService trackService;
    private final PlaylistService playlistService;

    public MainController(TrackService trackService,
                          PlaylistService playlistService) {
        this.trackService = trackService;
        this.playlistService = playlistService;
    }

    @GetMapping("/")
    public String showMainPage(Model model, @RequestParam(required = false) String search) {
        List<Track> tracks;
        if (search != null && !search.trim().isEmpty()) {
            tracks = trackService.searchTracks(search.trim());
            model.addAttribute("searchQuery", search.trim());
        } else {
            tracks = trackService.getAllTracks();
        }

        model.addAttribute("tracks", tracks);
        model.addAttribute("playlists", playlistService.getAllPlaylists());
        return "main";
    }
}
