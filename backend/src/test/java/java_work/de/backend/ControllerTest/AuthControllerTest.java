package java_work.de.backend.ControllerTest;

import java_work.de.backend.contoller.AuthController;
import java_work.de.backend.dto.UserPasswordUpdateDTO;
import java_work.de.backend.dto.UserRegistrationDTO;
import java_work.de.backend.dto.UserLoginDTO;
import java_work.de.backend.model.User;
import java_work.de.backend.service.SecurityConfig;
import java_work.de.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class) // Testet nur AuthController
@Import(SecurityConfig.class) // Lädt die echte SecurityConfig
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @MockBean
    AuthenticationManager authenticationManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void register_success() throws Exception {
        UserRegistrationDTO dto = new UserRegistrationDTO("new@example.com", "password123");

        when(userService.findByEmail(dto.email())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Registrierung erfolgreich!"));
    }
    @Test
    void register_shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
        UserRegistrationDTO dto = new UserRegistrationDTO("invalid-email", "pass");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @Test
    void register_error_emailExists() throws Exception {
        UserRegistrationDTO dto = new UserRegistrationDTO("bob@example.com", "secret123");

        when(userService.findByEmail(dto.email()))
                .thenReturn(Optional.of(new User("123", "bob@example.com", "pass", User.Role.ROLE_USER)));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("E-Mail existiert bereits!"));
    }

    @Test
    void login_success() throws Exception {
        UserLoginDTO dto = new UserLoginDTO("charlie@example.com", "secret123");

        Authentication authMock = Mockito.mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authMock);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Login erfolgreich!"));
    }

    @Test
    void updatePassword_success() throws Exception {
        UserPasswordUpdateDTO dto = new UserPasswordUpdateDTO("someNewPass");

        mockMvc.perform(put("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Deine Daten wurden erfolgreich aktualisiert!"));
    }

    @Test
    @WithMockUser(username = "eve@example.com")
    void updatePassword_userNotFound() throws Exception {
        UserPasswordUpdateDTO dto= new UserPasswordUpdateDTO("someNewPass");

        doThrow(new IllegalArgumentException("Benutzer nicht gefunden!"))
                .when(userService).updateUserPassword("eve@example.com", dto.newPassword());

        mockMvc.perform(put("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Benutzer nicht gefunden!"));
    }
}
