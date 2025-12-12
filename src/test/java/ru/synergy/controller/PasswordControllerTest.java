package ru.synergy.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.synergy.model.User;
import ru.synergy.repository.UserRepository;
import ru.synergy.config.PasswordEncoderConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PasswordEncoderConfig passwordEncoderConfig;

    @MockitoBean
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private Authentication authentication;
    private final String username = "testUser";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        authentication = new UsernamePasswordAuthenticationToken(username, "password");
    }

    @Test
    void showChangePasswordForm_shouldReturnViewWithUsername() throws Exception {
        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(new User()));

        mockMvc.perform(get("/password/change")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("change-password"))
                .andExpect(model().attribute("username", username));
    }

    @Test
    void changePassword_success() throws Exception {
        String currentPassword = "oldPass123";
        String newPassword = "newPass456";

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoderConfig.getPasswordEncoder().encode(currentPassword));

        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));

        MockHttpServletRequestBuilder request = post("/password/change")
                .param("currentPassword", currentPassword)
                .param("newPassword", newPassword)
                .param("confirmNewPassword", newPassword)
                .principal(authentication);

        mockMvc.perform(request)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Пароль успешно изменён"));

        verify(userRepository).save(user);
        assertThat(user.getPasswordHash())
                .isNotEqualTo(passwordEncoderConfig.getPasswordEncoder().encode(currentPassword));
    }

    @Test
    void changePassword_wrongCurrentPassword() throws Exception {
        String currentPassword = "oldPass123";
        String newPassword = "newPass456";

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoderConfig.getPasswordEncoder().encode(currentPassword));

        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(post("/password/change")
                        .param("currentPassword", "wrongPass")
                        .param("newPassword", newPassword)
                        .param("confirmNewPassword", newPassword)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("change-password"))
                .andExpect(model().attribute("error", "Текущий пароль не верный"));
    }

    @Test
    void changePassword_newPasswordDoesNotMatchRegex() throws Exception {
        String currentPassword = "oldPass123";

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(
                passwordEncoderConfig.getPasswordEncoder().encode(currentPassword)
        );

        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(post("/password/change")
                        .param("currentPassword", currentPassword)
                        .param("newPassword", "short")
                        .param("confirmNewPassword", "short")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("change-password"))
                .andExpect(model().attribute("error",
                        "Пароль должен содержать минимум 8 символов, включая буквы и цифры"));
    }


    @Test
    void changePassword_passwordsDoNotMatch() throws Exception {
        String currentPassword = "oldPass123";

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoderConfig.getPasswordEncoder().encode(currentPassword));

        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(post("/password/change")
                        .param("currentPassword", currentPassword)
                        .param("newPassword", "newPass123")
                        .param("confirmNewPassword", "differentPass")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("change-password"))
                .andExpect(model().attribute("error", "Новый пароль и подтверждение не совпадают"));
    }


    @Test
    void changePassword_userNotFound() throws Exception {
        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(post("/password/change")
                        .param("currentPassword", "oldPass123")
                        .param("newPassword", "newPass123")
                        .param("confirmNewPassword", "newPass123")
                        .principal(authentication))
                .andExpect(status().isUnauthorized());
    }
}
