package ru.synergy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.synergy.model.Playlist;
import ru.synergy.model.User;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUser(User user);
}
