package ru.synergy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.synergy.model.TrackReview;

import java.util.List;

public interface TrackReviewRepository extends JpaRepository<TrackReview, Long> {
    List<TrackReview> findByTrackId(Long trackId);

    @Query("SELECT avg(tr.rating) FROM TrackReview tr WHERE tr.track.id = :trackId")
    Double findAverageRating(Long trackId);

}
