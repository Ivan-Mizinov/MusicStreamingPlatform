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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.synergy.model.Role;
import ru.synergy.model.User;
import ru.synergy.service.UserService;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "administrator", roles = "ADMIN")
    void showUsers_ShouldReturnUsersPageWithCurrentUserAndAllUsers() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("administrator");
        currentUser.setRole(Role.ROLE_ADMIN);

        User user1 = new User();
        user1.setId(2L);
        user1.setUsername("FirstUser");
        user1.setRole(Role.ROLE_USER);

        when(userService.findByUsername("administrator")).thenReturn(Optional.of(currentUser));
        when(userService.findAll()).thenReturn(Arrays.asList(currentUser, user1));

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("users"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("currentUser"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("users"))
                .andExpect(MockMvcResultMatchers.model().attribute("currentUser", currentUser))
                .andExpect(MockMvcResultMatchers.model().attribute("users", Arrays.asList(currentUser, user1)));

        verify(userService).findByUsername("administrator");
        verify(userService).findAll();
    }

    @Test
    @WithMockUser(username = "administrator", roles = "ADMIN")
    void showUsers_WhenUserNotFound_ShouldThrowException() {
        when(userService.findByUsername("administrator")).thenReturn(Optional.empty());

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/users"));
            fail("Ожидалось исключение RuntimeException");
        } catch (Exception e) {
            assertInstanceOf(RuntimeException.class, e.getCause());
            assertEquals("Пользователь не найден", e.getCause().getMessage());
        }

        verify(userService).findByUsername("administrator");
    }


    @Test
    @WithMockUser(username = "administrator", roles = "ADMIN")
    void deleteUser_ShouldDeleteUserAndRedirectToUsers() throws Exception {
        doNothing().when(userService).deleteById(1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/1/delete"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/users"));

        verify(userService).deleteById(1L);
    }

    @Test
    @WithMockUser(username = "administrator", roles = "ADMIN")
    void updateRole_ShouldUpdateUserRoleAndRedirectToUsers() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setRole(Role.ROLE_USER);

        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(userService.save(user)).thenReturn(user);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/users/1/role")
                .param("role", "ROLE_ADMIN");

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/users"));


        verify(userService).findById(1L);
        verify(userService).save(user);
        assertEquals(Role.ROLE_ADMIN, user.getRole());
    }

    @Test
    @WithMockUser(username = "administrator", roles = "ADMIN")
    void updateRole_WhenUserNotFound_ShouldThrowException() {
        when(userService.findByUsername("administrator")).thenReturn(Optional.empty());

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/users"));
            fail("Ожидалось исключение RuntimeException");
        } catch (Exception e) {
            assertInstanceOf(RuntimeException.class, e.getCause());
            assertEquals("Пользователь не найден", e.getCause().getMessage());
        }

        verify(userService).findByUsername("administrator");
    }
}
