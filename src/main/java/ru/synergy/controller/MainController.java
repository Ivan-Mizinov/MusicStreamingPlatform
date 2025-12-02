package ru.synergy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.synergy.service.PlaylistService;
import ru.synergy.service.TrackService;

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
    public String showMainPage(Model model) {
        model.addAttribute("tracks", trackService.getAllTracks());
        model.addAttribute("playlists", playlistService.getAllPlaylists());
        return "main";
    }
}
