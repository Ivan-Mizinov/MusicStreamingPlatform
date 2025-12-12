package ru.synergy.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.synergy.model.Playlist;
import ru.synergy.model.Track;
import ru.synergy.model.User;
import ru.synergy.repository.UserRepository;
import ru.synergy.service.PlaylistService;
import ru.synergy.service.TrackService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PlaylistControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistService playlistService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TrackService trackService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void showCreateForm_shouldReturnCreateView() throws Exception {
        mockMvc.perform(get("/playlist/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("playlist/create"))
                .andExpect(model().attributeExists("playlist"));
    }

    @Test
    void createPlaylist_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        doNothing().when(playlistService).save(any(Playlist.class));

        MockHttpServletRequestBuilder request = post("/playlist/create")
                .param("name", "My Playlist")
                .principal(() -> "testUser");

        mockMvc.perform(request)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        verify(playlistService).save(argThat(p ->
                "My Playlist".equals(p.getName()) &&
                        user.equals(p.getUser())
        ));
    }

    @Test
    void createPlaylist_unauthorized() throws Exception {
        MockHttpServletRequestBuilder request = post("/playlist/create")
                .param("name", "My Playlist");

        mockMvc.perform(request)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("error", "Пользователь не авторизован"));
    }

    @Test
    void addTrackToPlaylist_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        Playlist playlist = new Playlist();
        playlist.setId(100L);
        playlist.setUser(user);

        Track track = new Track();
        track.setId(200L);
        track.setFileUrl("https://example.com/track.mp3");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(playlistService.getPlaylistById(100L)).thenReturn(Optional.of(playlist));
        when(trackService.getTrackByFileUrl("https://example.com/track.mp3")).thenReturn(Optional.of(track));

        MockHttpServletRequestBuilder request = post("/playlist/add-track")
                .param("fileUrl", "https://example.com/track.mp3")
                .param("playlistId", "100")
                .principal(() -> "testUser");

        mockMvc.perform(request)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Трек добавлен в плейлист!"));

        verify(playlistService).save(playlist);
        assertTrue(playlist.getTracks().contains(track));
    }

    @Test
    void addTrackToPlaylist_trackNotFound() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        Playlist playlist = new Playlist();
        playlist.setId(100L);
        playlist.setUser(user);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(playlistService.getPlaylistById(100L)).thenReturn(Optional.of(playlist));
        when(trackService.getTrackByFileUrl("bad-url")).thenReturn(Optional.empty());

        MockHttpServletRequestBuilder request = post("/playlist/add-track")
                .param("fileUrl", "bad-url")
                .param("playlistId", "100")
                .principal(() -> "testUser");

        mockMvc.perform(request)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Трек не найден по URL: bad-url"));
    }

    @Test
    void deletePlaylist_success() throws Exception {
        doNothing().when(playlistService).deletePlaylist(100L);

        mockMvc.perform(delete("/playlist/delete/100"))
                .andExpect(status().isNoContent());

        verify(playlistService).deletePlaylist(100L);
    }

    @Test
    void deletePlaylist_error() throws Exception {
        doThrow(new RuntimeException("DB error")).when(playlistService).deletePlaylist(100L);

        mockMvc.perform(delete("/playlist/delete/100"))
                .andExpect(status().isInternalServerError());

        verify(playlistService).deletePlaylist(100L);
    }
}