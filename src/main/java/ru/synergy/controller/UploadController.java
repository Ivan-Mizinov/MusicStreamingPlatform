package ru.synergy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.synergy.dto.TrackDto;
import ru.synergy.model.Track;
import ru.synergy.service.TrackService;

@Controller
public class UploadController {
    private final TrackService trackService;

    public UploadController(TrackService trackService) {
        this.trackService = trackService;
    }

    @PostMapping("/upload-track")
    public String uploadTrack(
            @ModelAttribute TrackDto trackDto,
            @RequestParam("file") MultipartFile file,
            Model model) {
        try {
            Track track = new Track();
            track.setTitle(trackDto.title());
            track.setArtist(trackDto.artist());

            Track savedTrack = trackService.saveTrack(track, file);

            model.addAttribute("success", "Трек успешно загружен!");
            model.addAttribute("track", savedTrack);

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при загрузке: " + e.getMessage());
        }
        return "upload-result";
    }

    @GetMapping("/upload")
    public String showUploadForm() {
        return "upload-form";
    }
}
