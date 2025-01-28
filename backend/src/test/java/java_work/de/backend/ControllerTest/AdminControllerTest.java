package java_work.de.backend.ControllerTest;

import java_work.de.backend.contoller.AdminController;
import java_work.de.backend.model.User;
import java_work.de.backend.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // <-- wichtig für PUT/POST
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Test
    @DisplayName("GET /api/admin/users - als ADMIN erfolgreich (200 OK) - liefert Liste")
    @WithMockUser(roles = "ADMIN")  // Simuliert, dass wir als Admin eingeloggt sind
    void getAllUsers_success() throws Exception {
        // Mock-Verhalten definieren: userService.findAllUsers() -> Liste mit 2 Usern zurückgeben
        List<User> mockUsers = List.of(
                new User("1", "alice@example.com", "pwHash", User.Role.ROLE_USER),
                new User("2", "admin@example.com", "pwHash", User.Role.ROLE_ADMIN)
        );
        when(userService.findAllUsers()).thenReturn(mockUsers);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());

        verify(userService).findAllUsers(); // überprüft, ob Service aufgerufen wurde
    }


    @Test
    @DisplayName("PUT /api/admin/users/{email}/role - Rolle erfolgreich geändert")
    @WithMockUser(roles = "ADMIN")
    void changeUserRole_success() throws Exception {
        // Angegebener User ist vorhanden
        User existingUser = new User("abc", "bob@example.com", "pwHash", User.Role.ROLE_USER);
        when(userService.findByEmail("bob@example.com")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(put("/api/admin/users/bob@example.com/role")
                        .with(csrf()) // PUT => CSRF
                        .param("role", "ROLE_ADMIN")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Rolle erfolgreich geändert!"));

        // Prüfen, ob userService.save(...) aufgerufen wurde
        verify(userService).save(any(User.class));
    }

    @Test
    @DisplayName("PUT /api/admin/users/{email}/role - Benutzer nicht gefunden => 400")
    @WithMockUser(roles = "ADMIN")
    void changeUserRole_userNotFound() throws Exception {
        // userService.findByEmail(...) -> empty
        when(userService.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/admin/users/unknown@example.com/role")
                        .with(csrf())
                        .param("role", "ROLE_ADMIN")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Benutzer nicht gefunden!"));
    }

    @Test
    @DisplayName("PUT /api/admin/users/{email}/role - Ungültige Rolle => 400")
    @WithMockUser(roles = "ADMIN")
    void changeUserRole_invalidRole() throws Exception {
        User existingUser = new User("abc", "bob@example.com", "pwHash", User.Role.ROLE_USER);
        when(userService.findByEmail("bob@example.com")).thenReturn(Optional.of(existingUser));

        // param("role", "ROLE_PINEAPPLE") => wirft Exception in valueOf()
        mockMvc.perform(put("/api/admin/users/bob@example.com/role")
                        .with(csrf())
                        .param("role", "ROLE_Tester") // Ungültig
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ungültige Rolle! Verfügbare Rollen: ROLE_ADMIN, ROLE_USER"));
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/email - erfolgreich E-Mail geändert")
    @WithMockUser(roles = "ADMIN")
    void updateUserEmail_success() throws Exception {
        // userService.updateUserEmailByAdmin(...) wirft keine Exception
        // => wir definieren kein spezielles when(...) => default: no exception

        mockMvc.perform(put("/api/admin/users/123/email")
                        .with(csrf())
                        .param("newEmail", "alice@new.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Benutzerdetails erfolgreich aktualisiert!"));

        // Prüfen, ob userService aufgerufen wurde
        verify(userService).updateUserEmailByAdmin("123", "alice@new.com");
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/email - Benutzer nicht gefunden => 400")
    @WithMockUser(roles = "ADMIN")
    void updateUserEmail_userNotFound() throws Exception {
        // userService.updateUserEmailByAdmin(...) => wirf IllegalArgumentException
        doThrow(new IllegalArgumentException("Benutzer nicht gefunden!"))
                .when(userService).updateUserEmailByAdmin("999", "bob@new.com");

        mockMvc.perform(put("/api/admin/users/999/email")
                        .with(csrf())
                        .param("newEmail", "bob@new.com"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Benutzer nicht gefunden!"));
    }
}
