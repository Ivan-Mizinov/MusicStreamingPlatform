package ru.synergy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.synergy.model.Playlist;
import ru.synergy.model.User;
import ru.synergy.repository.PlaylistRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PlaylistService {
    private final PlaylistRepository playlistRepository;

    public PlaylistService(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    public List<Playlist> getUserPlaylists(User user) {
        return playlistRepository.findByUser(user);
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
