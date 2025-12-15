package ru.synergy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.synergy.model.*;
import ru.synergy.repository.UserSubscriptionRepository;
import ru.synergy.service.PlaylistService;
import ru.synergy.service.TrackReviewService;
import ru.synergy.service.TrackService;
import ru.synergy.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MainControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrackService trackService;

    @MockitoBean
    private PlaylistService playlistService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TrackReviewService trackReviewService;

    @MockitoBean
    private UserSubscriptionRepository subRepo;

    @Test
    @WithMockUser(username = "testUser")
    void showMainPage_WithoutSearch_ShouldReturnMainViewWithData() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setRole(Role.ROLE_USER);

        Playlist playlist = new Playlist();
        playlist.setId(100L);
        playlist.setName("My Playlist");

        Track track = new Track();
        track.setId(200L);
        track.setTitle("Test Track");

        UserSubscription subscription = new UserSubscription();
        subscription.setEndDate(LocalDateTime.now().plusDays(30));

        when(userService.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(subRepo.findByUserAndIsActive(user, true)).thenReturn(Optional.of(subscription));
        when(trackService.getAllTracks()).thenReturn(Collections.singletonList(track));
        when(playlistService.getUserPlaylists(user)).thenReturn(Collections.singletonList(playlist));
        when(userService.findAll()).thenReturn(Collections.singletonList(user));
        when(trackReviewService.getReviews(200L)).thenReturn(Collections.emptyList());
        when(trackReviewService.getAverageRating(200L)).thenReturn(0.0);

        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attribute("hasActiveSubscription", true))
                .andExpect(model().attribute("daysLeft", 29L))
                .andExpect(model().attribute("tracks", Collections.singletonList(track)))
                .andExpect(model().attribute("playlists", Collections.singletonList(playlist)))
                .andExpect(model().attribute("users", Collections.singletonList(user)))
                .andExpect(model().attribute("followingIds", Collections.emptyList()))
                .andExpect(model().attribute("currentUserId", 1L))
                .andExpect(model().attributeExists("followedPlaylists"))
                .andExpect(model().attributeExists("reviewsByTrack"))
                .andExpect(model().attributeExists("avgRatings"));

        verify(userService).findByUsername("testUser");
        verify(subRepo).findByUserAndIsActive(user, true);
        verify(trackService).getAllTracks();
        verify(playlistService).getUserPlaylists(user);
        verify(userService).findAll();
    }

    @Test
    @WithMockUser(username = "testUser")
    void showMainPage_WithSearch_ShouldReturnFilteredTracks() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setRole(Role.ROLE_USER);

        Track track = new Track();
        track.setId(200L);
        track.setTitle("Search Result");

        when(userService.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(trackService.searchTracks("query")).thenReturn(Collections.singletonList(track));

        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .param("search", "query"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("searchQuery", "query"))
                .andExpect(model().attribute("tracks", Collections.singletonList(track)));

        verify(trackService).searchTracks("query");
    }

    @Test
    void showMainPage_WhenNotAuthenticated_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(userService, subRepo, trackService);
    }

    @Test
    @WithMockUser(username = "unknown")
    void showMainPage_WhenUserNotFound_ShouldShowError() throws Exception {
        when(userService.findByUsername("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("error", "Пользователь не найден"))
                .andExpect(model().attribute("hasActiveSubscription", false))
                .andExpect(model().attribute("daysLeft", 0));

        verify(userService).findByUsername("unknown");
    }

    @Test
    @WithMockUser(username = "testUser")
    void showPlaylistTracks_ValidPlaylist_ShouldReturnMainView() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setRole(Role.ROLE_USER);

        Playlist playlist = new Playlist();
        playlist.setId(100L);
        playlist.setName("Favorite Tracks");

        Track track = new Track();
        track.setId(200L);
        track.setTitle("Playlist Track");

        playlist.setTracks(Collections.singletonList(track));

        when(userService.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(playlistService.getPlaylistById(100L)).thenReturn(Optional.of(playlist));
        when(subRepo.findByUserAndIsActive(user, true)).thenReturn(Optional.empty());
        when(playlistService.getUserPlaylists(user)).thenReturn(Collections.singletonList(playlist));
        when(userService.findAll()).thenReturn(Collections.singletonList(user));
        when(trackReviewService.getReviews(200L)).thenReturn(Collections.emptyList());
        when(trackReviewService.getAverageRating(200L)).thenReturn(4.5);

        mockMvc.perform(MockMvcRequestBuilders.get("/playlist/100"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("playlistName", "Favorite Tracks"))
                .andExpect(model().attribute("activePlaylistId", 100L))
                .andExpect(model().attribute("tracks", Collections.singletonList(track)))
                .andExpect(model().attribute("hasActiveSubscription", false))
                .andExpect(model().attribute("daysLeft", 0))
                .andExpect(model().attributeExists("playlists"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("followingIds"))
                .andExpect(model().attributeExists("currentUserId"))
                .andExpect(model().attributeExists("followedPlaylists"));

        verify(playlistService).getPlaylistById(100L);
    }

    @Test
    void showPlaylistTracks_WhenNotAuthenticated_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/playlist/100"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andExpect(result -> verifyNoInteractions(userService, playlistService, subRepo));
    }

    @Test
    @WithMockUser(username = "testUser")
    void showPlaylistTracks_WhenUserNotFound_ShouldShowError() throws Exception {
        when(userService.findByUsername("testUser")).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/playlist/100"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(result -> {
                    verify(userService).findByUsername("testUser");
                    verifyNoMoreInteractions(playlistService);
                });
    }

    @Test
    @WithMockUser(username = "testUser")
    void showPlaylistTracks_WhenPlaylistNotFound() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setRole(Role.ROLE_USER);

        when(userService.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(playlistService.getPlaylistById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/playlist/999"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(result -> {
                    verify(playlistService).getPlaylistById(999L);
                    verify(userService).findByUsername("testUser");
                });
    }

    @Test
    @WithMockUser(username = "testUser")
    void showPlaylistTracks_WithActiveSubscription_ShouldIncludeDaysLeft() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setRole(Role.ROLE_USER);

        Playlist playlist = new Playlist();
        playlist.setId(100L);
        playlist.setName("Subscribed Playlist");

        Track track = new Track();
        track.setId(200L);

        playlist.setTracks(Collections.singletonList(track));

        UserSubscription subscription = new UserSubscription();
        subscription.setEndDate(LocalDateTime.now().plusDays(15));

        when(userService.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(subRepo.findByUserAndIsActive(user, true)).thenReturn(Optional.of(subscription));
        when(playlistService.getPlaylistById(100L)).thenReturn(Optional.of(playlist));
        when(playlistService.getUserPlaylists(user)).thenReturn(Collections.singletonList(playlist));
        when(userService.findAll()).thenReturn(Collections.singletonList(user));
        when(trackReviewService.getReviews(200L)).thenReturn(Collections.emptyList());
        when(trackReviewService.getAverageRating(200L)).thenReturn(3.0);

        mockMvc.perform(MockMvcRequestBuilders.get("/playlist/100"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("hasActiveSubscription", true))
                .andExpect(model().attribute("daysLeft", 14L));

        verify(subRepo).findByUserAndIsActive(user, true);
    }

    @Test
    @WithMockUser(username = "testUser")
    void showPlaylistTracks_ShouldPopulateFollowingIdsAndFollowedPlaylists() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setRole(Role.ROLE_USER);

        User followingUser = new User();
        followingUser.setId(2L);
        followingUser.setUsername("followed");

        user.setFollowing(Collections.singletonList(followingUser));

        Playlist playlist = new Playlist();
        playlist.setId(100L);
        playlist.setName("Shared Playlist");
        playlist.setUser(followingUser);

        Track track = new Track();
        track.setId(200L);

        playlist.setTracks(Collections.singletonList(track));

        when(userService.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(playlistService.getPlaylistById(100L)).thenReturn(Optional.of(playlist));
        when(playlistService.getPlaylistsOfUsers(user.getFollowing()))
                .thenReturn(Collections.singletonList(playlist));


        mockMvc.perform(MockMvcRequestBuilders.get("/playlist/100"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("followingIds", Collections.singletonList(2L)))
                .andExpect(model().attribute("followedPlaylists", Collections.singletonList(playlist)));


        verify(playlistService).getPlaylistsOfUsers(user.getFollowing());
    }
}
