package ru.synergy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.synergy.model.Track;

public interface TrackRepository extends JpaRepository<Track, Long> {
}
