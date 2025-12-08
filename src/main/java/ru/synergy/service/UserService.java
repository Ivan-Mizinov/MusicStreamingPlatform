package ru.synergy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.synergy.model.User;
import ru.synergy.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void follow(User follower, User target) {
        if (follower.getId().equals(target.getId())) {
            return;
        }
        if (!follower.getFollowing().contains(target)) {
            follower.getFollowing().add(target);
            userRepository.save(follower);
        }
    }

    @Transactional
    public void unfollow(User follower, User target) {
        follower.getFollowing().remove(target);
        userRepository.save(follower);
    }
}
