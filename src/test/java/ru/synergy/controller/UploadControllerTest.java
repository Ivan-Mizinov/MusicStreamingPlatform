package ru.synergy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;
import ru.synergy.model.Track;
import ru.synergy.service.TrackService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UploadControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrackService trackService;

    @Test
    @WithMockUser(username = "testUser")
    void uploadTrack_ShouldSaveTrackAndReturnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp3",
                "audio/mpeg",
                "file content".getBytes()
        );

        Track savedTrack = new Track();
        savedTrack.setId(1L);
        savedTrack.setTitle("New Track");
        savedTrack.setArtist("Artist");
        savedTrack.setGenres("Rock");

        when(trackService.saveTrack(any(Track.class), any(MultipartFile.class)))
                .thenReturn(savedTrack);

        MockMultipartHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/upload-track")
                .file(file)
                .param("title", "New Track")
                .param("artist", "Artist")
                .param("genre", "Rock");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("upload-result"))
                .andExpect(model().attribute("success", "Трек успешно загружен!"))
                .andExpect(model().attribute("track", savedTrack));

        verify(trackService).saveTrack(any(Track.class), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    void uploadTrack_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "faulty.mp3",
                "audio/mpeg",
                "bad content".getBytes()
        );

        when(trackService.saveTrack(any(Track.class), any(MultipartFile.class)))
                .thenThrow(new RuntimeException("Ошибка сохранения файла"));

        MockMultipartHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/upload-track")
                .file(file)
                .param("title", "Faulty Track")
                .param("artist", "Bad Artist")
                .param("genre", "Unknown");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("upload-result"))
                .andExpect(model().attribute("error", "Ошибка при загрузке: Ошибка сохранения файла"));

        verify(trackService).saveTrack(any(Track.class), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    void uploadTrack_WithoutFile_ShouldReturnError() throws Exception {
        MockMultipartHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/upload-track")
                .param("title", "No File Track")
                .param("artist", "Solo Artist")
                .param("genre", "Acoustic");

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trackService);
    }

    @Test
    @WithMockUser(username = "testUser")
    void showUploadForm_ShouldReturnUploadFormView() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/upload"))
                .andExpect(status().isOk())
                .andExpect(view().name("upload-form"));

        verifyNoInteractions(trackService);
    }

    @Test
    @WithMockUser(username = "testUser")
    void uploadTrack_WithEmptyTitle_ShouldHandleGracefully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "emptyTitle.mp3",
                "audio/mpeg",
                "content".getBytes()
        );

        MockMultipartHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/upload-track")
                .file(file)
                .param("title", "")
                .param("artist", "Some Artist")
                .param("genre", "Some Genre");

        when(trackService.saveTrack(any(Track.class), any(MultipartFile.class)))
                .thenReturn(new Track());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("upload-result"));

        verify(trackService).saveTrack(any(Track.class), any(MultipartFile.class));
    }
}