package ru.synergy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.synergy.model.Playlist;
import ru.synergy.model.User;
import ru.synergy.repository.PlaylistRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlaylistServiceTest {
    @Mock
    private PlaylistRepository playlistRepository;

    @InjectMocks
    private PlaylistService playlistService;

    private User user;
    private Playlist playlist;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        playlist = new Playlist();
        playlist.setId(1L);
        playlist.setName("Favourite Songs");
        playlist.setUser(user);
    }

    @Test
    void getUserPlaylists_shouldReturnPlaylistsForUser() {
        List<Playlist> playlists = List.of(playlist);
        when(playlistRepository.findByUser(user)).thenReturn(playlists);

        List<Playlist> result = playlistService.getUserPlaylists(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Favourite Songs");
        verify(playlistRepository).findByUser(user);
    }

    @Test
    void getPlaylistsOfUsers_shouldReturnPlaylistsForUsers() {
        List<User> users = List.of(user);
        List<Playlist> playlists = List.of(playlist);
        when(playlistRepository.findByUserIn(users)).thenReturn(playlists);

        List<Playlist> result = playlistService.getPlaylistsOfUsers(users);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(1L);
        verify(playlistRepository).findByUserIn(users);
    }

    @Test
    void getPlaylistsOfUsers_shouldReturnEmptyListForEmptyUsers() {
        List<Playlist> result = playlistService.getPlaylistsOfUsers(List.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(playlistRepository);
    }

    @Test
    void save_shouldSavePlaylist() {
        when(playlistRepository.save(playlist)).thenReturn(playlist);

        playlistService.save(playlist);

        verify(playlistRepository).save(playlist);
    }

    @Test
    void deletePlaylist_shouldDeletePlaylistWhenExists() {
        when(playlistRepository.existsById(1L)).thenReturn(true);

        playlistService.deletePlaylist(1L);

        verify(playlistRepository).existsById(1L);
        verify(playlistRepository).deleteById(1L);
    }

    @Test
    void deletePlaylist_shouldThrowExceptionWhenNotExists() {
        when(playlistRepository.existsById(999L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playlistService.deletePlaylist(999L)
        );

        assertThat(exception.getMessage()).isEqualTo("Плейлист не найден");
        verify(playlistRepository).existsById(999L);
        verify(playlistRepository, never()).deleteById(999L);
    }

    @Test
    void getPlaylistById_shouldReturnPlaylistWhenExists() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));

        Optional<Playlist> result = playlistService.getPlaylistById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(playlistRepository).findById(1L);
    }

    @Test
    void getPlaylistById_shouldReturnEmptyWhenNotExists() {
        when(playlistRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Playlist> result = playlistService.getPlaylistById(999L);

        assertThat(result).isEmpty();
        verify(playlistRepository).findById(999L);
    }
}
