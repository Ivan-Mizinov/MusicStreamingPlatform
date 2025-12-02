package ru.synergy.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.synergy.model.Track;
import ru.synergy.repository.TrackRepository;

import java.util.List;

@Service
public class TrackService {
    private final TrackRepository trackRepository;
    private final MinioService minioService;

    public TrackService(TrackRepository trackRepository, MinioService minioService) {
        this.trackRepository = trackRepository;
        this.minioService = minioService;
    }

    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public Track saveTrack(Track track, MultipartFile file) throws Exception {
        minioService.uploadFile("music-bucket", file.getOriginalFilename(), file);
        track.setFileUrl(minioService.getFileUrl("music-bucket", file.getOriginalFilename()));
        return trackRepository.save(track);
    }
}
