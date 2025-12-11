package ru.synergy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.synergy.model.Track;
import ru.synergy.model.TrackReview;
import ru.synergy.model.User;
import ru.synergy.repository.TrackRepository;
import ru.synergy.repository.TrackReviewRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrackReviewServiceTest {
    @Mock
    private TrackReviewRepository trackReviewRepository;

    @Mock
    private TrackRepository trackRepository;

    @InjectMocks
    private TrackReviewService trackReviewService;

    private User user;
    private Track track;
    private TrackReview review1;
    private TrackReview review2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        track = new Track();
        track.setId(100L);
        track.setTitle("Test Track");

        review1 = new TrackReview();
        review1.setId(1L);
        review1.setTrack(track);
        review1.setRating(5);
        review1.setComment("Отличный трек!");
        review1.setCreatedAt(LocalDateTime.now());

        review2 = new TrackReview();
        review2.setId(1L);
        review2.setTrack(track);
        review2.setRating(4);
        review2.setComment("Хороший трек");
        review2.setCreatedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    void getReviews_shouldReturnListOfReviewsForTrack() {
        when(trackReviewRepository.findByTrackId(100L))
                .thenReturn(Arrays.asList(review1, review2));

        List<TrackReview> reviews = trackReviewService.getReviews(100L);

        assertThat(reviews).hasSize(2);
        assertThat(reviews.get(0).getRating()).isEqualTo(5);
        assertThat(reviews.get(1).getRating()).isEqualTo(4);

        verify(trackReviewRepository).findByTrackId(100L);
    }

    @Test
    void getAverageRating_shouldReturnRoundedAverage() {
        when(trackReviewRepository.findAverageRating(100L)).thenReturn(4.5);

        Double avg = trackReviewService.getAverageRating(100L);

        assertThat(avg).isEqualTo(4.5);
        verify(trackReviewRepository).findAverageRating(100L);
    }


    @Test
    void getAverageRating_shouldReturnNullIfNoReviews() {
        when(trackReviewRepository.findAverageRating(100L)).thenReturn(null);

        Double avg = trackReviewService.getAverageRating(100L);

        assertThat(avg).isNull();
        verify(trackReviewRepository).findAverageRating(100L);
    }

    @Test
    void addReview_shouldSaveReviewWhenTrackExistsAndRatingValid() {
        when(trackRepository.findById(100L)).thenReturn(Optional.of(track));
        when(trackReviewRepository.save(any(TrackReview.class))).thenAnswer(invocation -> {
            TrackReview saved = invocation.getArgument(0);
            saved.setId(999L);
            return saved;
        });

        trackReviewService.addReview(100L, user, 5, "Новый отзыв");

        ArgumentCaptor<TrackReview> captor = ArgumentCaptor.forClass(TrackReview.class);
        verify(trackReviewRepository).save(captor.capture());

        TrackReview savedReview = captor.getValue();
        assertThat(savedReview.getTrack()).isEqualTo(track);
        assertThat(savedReview.getUser()).isEqualTo(user);
        assertThat(savedReview.getRating()).isEqualTo(5);
        assertThat(savedReview.getComment()).isEqualTo("Новый отзыв");
        assertThat(savedReview.getCreatedAt()).isNotNull();
    }

    @Test
    void addReview_shouldThrowExceptionIfTrackNotFound() {
        when(trackRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
                (IllegalArgumentException.class),
                () -> trackReviewService.addReview(999L, user, 5, "Отзыв"));

        assertThat(exception.getMessage()).isEqualTo("Трек не найден");
        verify(trackRepository).findById(999L);
        verify(trackReviewRepository, never()).save(any());
    }

    @Test
    void addReview_shouldThrowExceptionIfRatingOutOfRange() {
        when(trackRepository.findById(100L)).thenReturn(Optional.of(track));

        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows
                (IllegalArgumentException.class,
                        () -> trackReviewService.addReview(100L, user, 6, "Отзыв"));

        assertThat(exception.getMessage()).isEqualTo("Оценка должна быть от 1 до 5");
        verify(trackRepository).findById(100L);
        verify(trackReviewRepository, never()).save(any());
    }

    @Test
    void addReview_shouldAllowNullRating() {
        when(trackRepository.findById(100L)).thenReturn(Optional.of(track));
        when(trackReviewRepository.save(any(TrackReview.class))).thenAnswer(i -> i.getArgument(0));

        trackReviewService.addReview(100L, user, null, "Без оценки");

        ArgumentCaptor<TrackReview> captor = ArgumentCaptor.forClass(TrackReview.class);
        verify(trackReviewRepository).save(captor.capture());

        TrackReview saved = captor.getValue();
        assertThat(saved.getRating()).isNull();
    }
}
