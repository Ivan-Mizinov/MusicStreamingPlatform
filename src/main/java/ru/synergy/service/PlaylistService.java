package ru.synergy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.synergy.model.Playlist;
import ru.synergy.model.User;
import ru.synergy.repository.PlaylistRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaylistService {
    private final PlaylistRepository playlistRepository;

    public List<Playlist> getUserPlaylists(User user) {
        return playlistRepository.findByUser(user);
    }

    public List<Playlist> getPlaylistsOfUsers(List<User> users) {
        return users.isEmpty() ? List.of() : playlistRepository.findByUserIn(users);
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
