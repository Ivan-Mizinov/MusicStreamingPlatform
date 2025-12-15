package ru.synergy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.synergy.model.User;
import ru.synergy.service.TrackReviewService;
import ru.synergy.service.UserService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TrackReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrackReviewService trackReviewService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "testUser")
    void addReview_ShouldAddReviewAndRedirectWithSuccess() throws Exception {
        Long trackId = 1L;
        Integer rating = 5;
        String comment = "Отличный трек!";
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        when(userService.findByUsername("testUser")).thenReturn(java.util.Optional.of(user));
        doNothing().when(trackReviewService).addReview(trackId, user, rating, comment);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/tracks/1/review")
                .param("rating", "5")
                .param("comment", "Отличный трек!");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Отзыв сохранен"));

        verify(userService).findByUsername("testUser");
        verify(trackReviewService).addReview(trackId, user, rating, comment);
    }

    @Test
    void addReview_WhenNotAuthenticated_ShouldRedirectToLoginWithError() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/tracks/1/review");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(userService, trackReviewService);
    }

    @Test
    @WithMockUser(username = "unknownUser")
    void addReview_WhenUserNotFound_ShouldRedirectWithError() throws Exception {
        when(userService.findByUsername("unknownUser")).thenReturn(java.util.Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/tracks/1/review")
                .param("rating", "5");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Пользователь не найден"));

        verify(userService).findByUsername("unknownUser");
        verifyNoMoreInteractions(trackReviewService);
    }

    @Test
    @WithMockUser(username = "testUser")
    void addReview_WhenServiceThrowsException_ShouldRedirectWithError() throws Exception {
        Long trackId = 1L;
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        when(userService.findByUsername("testUser")).thenReturn(java.util.Optional.of(user));
        doThrow(new IllegalArgumentException("Ошибка при сохранении отзыва"))
                .when(trackReviewService).addReview(
                        eq(trackId),
                        eq(user),
                        any(),
                        any()
                );


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/tracks/1/review")
                .param("rating", "5");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Ошибка при сохранении отзыва"));

        verify(userService).findByUsername("testUser");
        verify(trackReviewService).addReview(
                eq(trackId),
                eq(user),
                any(),
                any()
        );

    }

    @Test
    @WithMockUser(username = "testUser")
    void addReview_WithOnlyTrackId_ShouldAddReviewWithoutRatingAndComment() throws Exception {
        Long trackId = 1L;
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        when(userService.findByUsername("testUser")).thenReturn(java.util.Optional.of(user));
        doNothing().when(trackReviewService).addReview(trackId, user, null, null);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/tracks/1/review");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Отзыв сохранен"));

        verify(userService).findByUsername("testUser");
        verify(trackReviewService).addReview(trackId, user, null, null);
    }
}