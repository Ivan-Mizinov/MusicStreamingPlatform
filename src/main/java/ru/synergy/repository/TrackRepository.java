package ru.synergy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.synergy.model.Track;

import java.util.List;

public interface TrackRepository extends JpaRepository<Track, Long> {
    @Query("SELECT t FROM Track t WHERE " +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.artist) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.genres) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Track> searchTracks(String query);
}
