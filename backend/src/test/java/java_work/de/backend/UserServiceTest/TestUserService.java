package java_work.de.backend.UserServiceTest;

import java_work.de.backend.model.User;
import java_work.de.backend.repo.UserRepository;
import java_work.de.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TestUserService {

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Initialisiert die Mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_Success() {
        //Given
        // 1. Wir mocken, dass die E-Mail "test@test.com" NICHT existiert.
        // 2. Wir erwarten, dass userRepository.save(...) aufgerufen wird.
        String email = "test@test.com";
        String password = "test";
        User.Role role = User.Role.ROLE_USER;

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("secret");


        //When
        userService.registerUser(email, password, role);

        //Then
        // verify() prüft, ob userRepository.save(...) genau 1x aufgerufen wird.
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrowException_WhenEmailAlreadyExists() {
        // Given
        String email = "alice@example.com";
        String password = "secret";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User(null, email, "hashed", User.Role.ROLE_USER)));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(email, password, User.Role.ROLE_USER));
    }

    @Test
    void updateUserPassword_Success() {
        // Given
        String email = "bob@example.com";
        String newPw = "newSecret";

        User existingUser = new User("123", email, "oldHashedPw", User.Role.ROLE_USER);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(newPw)).thenReturn("newHashedPw");

        // When
        userService.updateUserPassword(email, newPw);

        // Then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("newHashedPw", saved.password());
    }

    @Test
    void updateUserPassword_UserNotFound() {
        // Given
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.updateUserPassword("unknown@example.com", "secret"));
    }

}
