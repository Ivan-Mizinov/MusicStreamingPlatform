package ru.synergy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.synergy.model.Track;
import ru.synergy.repository.TrackRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrackService {
    private final TrackRepository trackRepository;
    private final MinioService minioService;

    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public List<Track> searchTracks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllTracks();
        }
        return trackRepository.searchTracks(query.trim());
    }

    public Track saveTrack(Track track, MultipartFile file) throws Exception {
        minioService.uploadFile("music-bucket", file.getOriginalFilename(), file);
        track.setFileUrl(minioService.getFileUrl("music-bucket", file.getOriginalFilename()));
        return trackRepository.save(track);
    }

    public Optional<Track> getTrackByFileUrl(String fileUrl) {
        return trackRepository.findByFileUrl(fileUrl);
    }
}
