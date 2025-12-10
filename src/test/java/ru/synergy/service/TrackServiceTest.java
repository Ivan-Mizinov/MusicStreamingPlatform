package ru.synergy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.synergy.model.Track;
import ru.synergy.repository.TrackRepository;

@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private TrackService trackService;

    private Track track;
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        track = new Track();
        track.setId(1L);
        track.setTitle("Test Track");
        track.setArtist("Test Artist");
        track.setGenres("rock");

        file = mock(MultipartFile.class);
    }

    @Test
    void getAllTracks_shouldReturnAllTracks() {
        List<Track> tracks = Collections.singletonList(track);
        when(trackRepository.findAll()).thenReturn(tracks);

        List<Track> result = trackService.getAllTracks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Track");
        verify(trackRepository).findAll();
    }

    @Test
    void searchTracks_withNullQuery_shouldReturnAllTracks() {
        List<Track> tracks = Collections.singletonList(track);
        when(trackRepository.findAll()).thenReturn(tracks);

        List<Track> result = trackService.searchTracks(null);

        assertThat(result).hasSize(1);
        verify(trackRepository).findAll();
    }

    @Test
    void searchTracks_withEmptyQuery_shouldReturnAllTracks() {
        List<Track> tracks = Collections.singletonList(track);
        when(trackRepository.findAll()).thenReturn(tracks);

        List<Track> result = trackService.searchTracks("   ");

        assertThat(result).hasSize(1);
        verify(trackRepository).findAll();
    }

    @Test
    void searchTracks_withValidQuery_shouldReturnFilteredTracks() {
        List<Track> tracks = Collections.singletonList(track);
        when(trackRepository.searchTracks("Test")).thenReturn(tracks);

        List<Track> result = trackService.searchTracks("Test");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Track");
        verify(trackRepository).searchTracks("Test");
    }

    @Test
    void saveTrack_shouldUploadFileAndSaveTrack() throws Exception {
        when(file.getOriginalFilename()).thenReturn("test.mp3");

        String fileUrl = "http://localhost:9000/music-bucket/test.mp3";
        doNothing().when(minioService).uploadFile(anyString(), anyString(), any(MultipartFile.class));
        when(minioService.getFileUrl(anyString(), anyString())).thenReturn(fileUrl);
        when(trackRepository.save(track)).thenReturn(track);

        Track result = trackService.saveTrack(track, file);

        assertThat(result.getFileUrl()).isEqualTo(fileUrl);
        verify(minioService).uploadFile("music-bucket", "test.mp3", file);
        verify(minioService).getFileUrl("music-bucket", "test.mp3");
        verify(trackRepository).save(track);
    }

    @Test
    void saveTrack_whenUploadFails_shouldThrowException() throws Exception {
        when(file.getOriginalFilename()).thenReturn("test.mp3");

        doThrow(new IOException("Minio error"))
                .when(minioService)
                .uploadFile(anyString(), anyString(), any(MultipartFile.class));

        assertThrows(Exception.class, () -> trackService.saveTrack(track, file));
        verify(minioService).uploadFile("music-bucket", "test.mp3", file);
        verify(trackRepository, never()).save(any(Track.class));
    }

    @Test
    void getTrackByFileUrl_shouldReturnTrackWhenFound() {
        when(trackRepository.findByFileUrl("http://url")).thenReturn(Optional.of(track));

        Optional<Track> result = trackService.getTrackByFileUrl("http://url");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(trackRepository).findByFileUrl("http://url");
    }

    @Test
    void getTrackByFileUrl_shouldReturnEmptyWhenNotFound() {
        when(trackRepository.findByFileUrl("http://unknown")).thenReturn(Optional.empty());

        Optional<Track> result = trackService.getTrackByFileUrl("http://unknown");

        assertThat(result).isEmpty();
        verify(trackRepository).findByFileUrl("http://unknown");
    }
}
