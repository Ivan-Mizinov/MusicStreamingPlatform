package ru.synergy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.synergy.model.User;
import ru.synergy.service.UserService;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "follower")
    void follow_ShouldFollowUserAndRedirectWithSuccess() throws Exception {
        Long targetId = 2L;
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("follower");

        User targetUser = new User();
        targetUser.setId(targetId);
        targetUser.setUsername("target");

        when(userService.findByUsername("follower")).thenReturn(Optional.of(currentUser));
        when(userService.findById(targetId)).thenReturn(Optional.of(targetUser));
        doNothing().when(userService).follow(currentUser, targetUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/2/follow"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Подписка сохранена"));

        verify(userService).findByUsername("follower");
        verify(userService).findById(targetId);
        verify(userService).follow(currentUser, targetUser);
    }

    @Test
    void follow_WhenNotAuthenticated_ShouldRedirectToLoginWithError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users/2/follow"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "testUser")
    void follow_WhenUserNotFound_ShouldRedirectWithError() throws Exception {
        Long targetId = 2L;

        when(userService.findByUsername("testUser")).thenReturn(Optional.empty());
        when(userService.findById(targetId)).thenReturn(Optional.of(new User()));

        mockMvc.perform(MockMvcRequestBuilders.post("/users/2/follow"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Пользователь не найден"));

        verify(userService).findByUsername("testUser");
        verify(userService).findById(targetId);
    }

    @Test
    @WithMockUser(username = "follower")
    void follow_WhenTargetUserNotFound_ShouldRedirectWithError() throws Exception {
        Long targetId = 999L;
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("follower");

        when(userService.findByUsername("follower")).thenReturn(Optional.of(currentUser));
        when(userService.findById(targetId)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/users/999/follow"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Пользователь не найден"));

        verify(userService).findByUsername("follower");
        verify(userService).findById(targetId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(username = "unfollower")
    void unfollow_ShouldUnfollowUserAndRedirectWithSuccess() throws Exception {
        Long targetId = 2L;
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("unfollower");

        User targetUser = new User();
        targetUser.setId(targetId);
        targetUser.setUsername("target");

        when(userService.findByUsername("unfollower")).thenReturn(Optional.of(currentUser));
        when(userService.findById(targetId)).thenReturn(Optional.of(targetUser));
        doNothing().when(userService).unfollow(currentUser, targetUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/2/unfollow"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Вы отписались"));

        verify(userService).findByUsername("unfollower");
        verify(userService).findById(targetId);
        verify(userService).unfollow(currentUser, targetUser);
    }

    @Test
    void unfollow_WhenNotAuthenticated_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users/2/unfollow"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "unknown")
    void unfollow_WhenUserNotFound_ShouldRedirectWithError() throws Exception {
        Long targetId = 2L;

        when(userService.findByUsername("unknown")).thenReturn(Optional.empty());
        when(userService.findById(targetId)).thenReturn(Optional.of(new User()));

        mockMvc.perform(MockMvcRequestBuilders.post("/users/2/unfollow"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Пользователь не найден"));

        verify(userService).findByUsername("unknown");
        verify(userService).findById(targetId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(username = "unfollower")
    void unfollow_WhenTargetUserNotFound_ShouldRedirectWithError() throws Exception {
        Long targetId = 999L;
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("unfollower");

        when(userService.findByUsername("unfollower")).thenReturn(Optional.of(currentUser));
        when(userService.findById(targetId)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/users/999/unfollow"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Пользователь не найден"));

        verify(userService).findByUsername("unfollower");
        verify(userService).findById(targetId);
        verifyNoMoreInteractions(userService);
    }
}