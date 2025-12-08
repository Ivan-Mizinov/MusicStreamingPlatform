package ru.synergy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.synergy.model.Track;
import ru.synergy.model.TrackReview;
import ru.synergy.model.User;
import ru.synergy.repository.TrackRepository;
import ru.synergy.repository.TrackReviewRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackReviewService {
    private final TrackReviewRepository trackReviewRepository;
    private final TrackRepository trackRepository;

    public List<TrackReview> getReviews(Long trackId) {
        return trackReviewRepository.findByTrackId(trackId);
    }

    public Double getAverageRating(Long trackId) {
        Double avg = trackReviewRepository.findAverageRating(trackId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : null;
    }

    @Transactional
    public void addReview(Long trackId, User user, Integer rating, String comment) {
        Track track = trackRepository.findById(trackId).orElseThrow(() -> new IllegalArgumentException("Трек не найден"));
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("Оценка должна быть от 1 до 5");
        }
        TrackReview review = new TrackReview();
        review.setTrack(track);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        trackReviewRepository.save(review);
    }
}
