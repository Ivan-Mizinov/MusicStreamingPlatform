package ru.synergy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.synergy.model.Playlist;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
}
