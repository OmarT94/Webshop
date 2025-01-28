package java_work.de.backend.ControllerTest;

import java_work.de.backend.contoller.AuthController;
import java_work.de.backend.model.User;
import java_work.de.backend.service.SecurityConfig;
import java_work.de.backend.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class) // Testet NUR AuthController
@Import(SecurityConfig.class) // Ladet deine echte SecurityConfig
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    // Beans, die der Controller injiziert bekommt, müssen gemockt werden
    @MockBean
    UserService userService;

    @MockBean
    AuthenticationManager authenticationManager;

    @Test
    void register_success() throws Exception {
        // userService.findByEmail(...) => Optional.empty() => E-Mail existiert noch nicht
        when(userService.findByEmail("alice@example.com"))
                .thenReturn(Optional.empty());
        mockMvc.perform(post("/api/auth/register")

                        .param("email", "alice@example.com")
                        .param("password", "secret123")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Registrierung erfolgreich!"));
    }

    @Test
    void register_error_emailExists() throws Exception {
        // E-Mail schon vorhanden => Optional.of(...)
        when(userService.findByEmail("bob@example.com"))
                .thenReturn(Optional.of(new User("123", "bob@example.com", "pass", User.Role.ROLE_USER)));

        mockMvc.perform(post("/api/auth/register")
                        .param("email", "bob@example.com")
                        .param("password", "secret123")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("E-Mail existiert bereits!"));
    }

    @Test
    void login_success() throws Exception {
        // authenticationManager.authenticate(...) => wir mocken Return
        Authentication authMock = Mockito.mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authMock);

        mockMvc.perform(post("/api/auth/login")
                        .param("email", "charlie@example.com")
                        .param("password", "secret123")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Login erfolgreich!"));
    }

    @Test

    void updatePassword_success() throws Exception {
        // Keine Exception => ok

        mockMvc.perform(put("/api/auth/me")
                        .param("newPassword", "someNewPass")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Deine Daten wurden erfolgreich aktualisiert!"));
    }

    @Test

    @WithMockUser(username = "eve@example.com")
    void updatePassword_userNotFound() throws Exception {
        // userService.updateUserPassword(...) => wirf Exception
        doThrow(new IllegalArgumentException("Benutzer nicht gefunden!"))
                .when(userService).updateUserPassword("eve@example.com", "someNewPass");

        mockMvc.perform(put("/api/auth/me")
                        .param("newPassword", "someNewPass")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Benutzer nicht gefunden!"));
    }
}
