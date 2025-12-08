package ru.synergy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.synergy.model.Playlist;
import ru.synergy.model.Track;
import ru.synergy.model.User;
import ru.synergy.repository.PlaylistRepository;
import ru.synergy.repository.TrackRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;

    public PlaylistService(PlaylistRepository playlistRepository, TrackRepository trackRepository) {
        this.playlistRepository = playlistRepository;
        this.trackRepository = trackRepository;
    }

    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll();
    }

    public List<Playlist> getUserPlaylists(User user) {
        return playlistRepository.findByUser(user);
    }

    public Playlist createPlaylist(String name, List<Long> trackIds, User user) {
        List<Track> tracks = trackRepository.findAllById(trackIds);

        Set<Long> foundIds = tracks.stream()
                .map(Track::getId)
                .collect(Collectors.toSet());
        Set<Long> requestedIds = new HashSet<>(trackIds);
        Set<Long> missingIds = requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toSet());

        if (!missingIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Не найдены треки с ID: " + missingIds
            );
        }

        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setTracks(tracks);
        playlist.setUser(user);

        return playlistRepository.save(playlist);
    }

    @Transactional
    public void save(Playlist playlist) {
        playlistRepository.save(playlist);
    }

    public void deletePlaylist(Long id) {
        if (!playlistRepository.existsById(id)) {
            throw new IllegalArgumentException("Плейлист не найден");
        }
        playlistRepository.deleteById(id);
    }

    public Optional<Playlist> getPlaylistById(Long id) {
        return playlistRepository.findById(id);
    }
}
