package ru.synergy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.synergy.model.User;
import ru.synergy.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    private User follower;
    private User target;

    @BeforeEach
    void setUp() {
        follower = new User();
        follower.setId(1L);
        follower.setUsername("follower");
        follower.setFollowing(new java.util.ArrayList<>());

        target = new User();
        target.setId(2L);
        target.setUsername("target");
    }

    @Test
    void findByUsername_shouldReturnUserWhenUserFound() {
        String username = "follower";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(follower));

        Optional<User> result = userService.findByUsername(username);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenUserNotFound() {
        String username = "unknown";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername(username);

        assertThat(result).isEmpty();
        verify(userRepository).findByUsername(username);
    }

    @Test
    void findById_shouldReturnUserWhenFound() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.of(follower));

        Optional<User> result = userService.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        verify(userRepository).findById(id);
    }

    @Test
    void findById_shouldReturnEmptyWhenUserNotFound() {
        Long id = 99L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(id);

        assertThat(result).isEmpty();
        verify(userRepository).findById(id);
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        List<User> users = List.of(follower, target);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(target, follower);
        verify(userRepository).findAll();
    }

    @Test
    void follow_shouldAddTargetToFollowingListAndSave() {
        userService.follow(follower, target);

        assertThat(follower.getFollowing()).contains(target);
        verify(userRepository).save(follower);
    }

    @Test
    void follow_shouldNotAddWhenSameUser() {
        userService.follow(follower, follower);

        assertThat(follower.getFollowing()).isEmpty();
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void follow__shouldNotAddDuplicate() {
        follower.getFollowing().add(target);
        userService.follow(follower, target);

        assertThat(follower.getFollowing()).hasSize(1);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void unfollow_shouldRemoveTargetFromFollowingAndSave() {
        follower.getFollowing().add(target);

        userService.unfollow(follower, target);

        assertThat(follower.getFollowing()).doesNotContain(target);
        verify(userRepository).save(follower);
    }

    @Test
    void deleteById_shouldCallRepositoryDelete() {
        Long id = 1L;

        userService.deleteById(id);

        verify(userRepository).deleteById(id);
    }

    @Test
    void save_shouldReturnSavedUser() {
        when(userRepository.save(follower)).thenReturn(follower);

        User result = userService.save(follower);

        assertThat(result).isEqualTo(follower);
        verify(userRepository).save(follower);
    }
}