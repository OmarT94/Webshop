//package java_work.de.backend.ControllerTest;
//
//import java_work.de.backend.contoller.AdminController;
//import java_work.de.backend.model.User;
//import java_work.de.backend.service.JwtUtil;
//import java_work.de.backend.service.UserService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(AdminController.class)
//class AdminControllerTest {
//    @MockBean
//    JwtUtil jwtUtil;  // Mocking für JwtUtil, um Fehler zu vermeiden
//
//    @Autowired
//    MockMvc mockMvc;
//
//    @MockBean
//    UserService userService;
//
//    @Test
//    @DisplayName("GET /api/admin/users - als ADMIN erfolgreich (200 OK)")
//    @WithMockUser(username = "admin@example.com", roles = "ADMIN") // Sicherstellen, dass Admin-Rolle gesetzt ist
//    void getAllUsers_success() throws Exception {
//        List<User> mockUsers = List.of(
//                new User("alice@example.com", "pwHash", "test","test",User.Role.ROLE_USER),
//                new User( "admin@example.com", "pwHash","test","test" ,User.Role.ROLE_ADMIN)
//        );
//        when(userService.findAllUsers()).thenReturn(mockUsers);
//
//        mockMvc.perform(get("/api/admin/users"))
//                .andExpect(status().isOk());
//
//        verify(userService).findAllUsers();
//    }
//
//    @Test
//    @DisplayName("PUT /api/admin/users/{email}/role - Rolle erfolgreich geändert")
//    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
//    void changeUserRole_success() throws Exception {
//        User existingUser = new User( "bob@example.com", "pwHash","test","test" ,User.Role.ROLE_USER);
//        when(userService.findByEmail("bob@example.com")).thenReturn(Optional.of(existingUser));
//
//        mockMvc.perform(put("/api/admin/users/bob@example.com/role")
//                        .with(csrf()) // PUT benötigt CSRF
//                        .param("role", "ROLE_ADMIN"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Rolle erfolgreich geändert!"));
//
//        verify(userService).saveUser(any(User.class));
//    }
//
//    @Test
//    @DisplayName("PUT /api/admin/users/{email}/role - Benutzer nicht gefunden (400)")
//    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
//    void changeUserRole_userNotFound() throws Exception {
//        when(userService.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
//
//        mockMvc.perform(put("/api/admin/users/unknown@example.com/role")
//                        .with(csrf())
//                        .param("role", "ROLE_ADMIN"))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("Benutzer nicht gefunden!"));
//    }
//
//    @Test
//    @DisplayName("PUT /api/admin/users/{email}/role - Ungültige Rolle (400)")
//    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
//    void changeUserRole_invalidRole() throws Exception {
//        User existingUser = new User( "bob@example.com", "pwHash","test","test" ,User.Role.ROLE_USER);
//        when(userService.findByEmail("bob@example.com")).thenReturn(Optional.of(existingUser));
//
//        mockMvc.perform(put("/api/admin/users/bob@example.com/role")
//                        .with(csrf())
//                        .param("role", "ROLE_INVALID")) // Ungültige Rolle
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("Ungültige Rolle! Verfügbare Rollen: ROLE_ADMIN, ROLE_USER"));
//    }
//
//    @Test
//    @DisplayName("PUT /api/admin/users/{id}/email - Erfolgreiche Änderung")
//    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
//    void updateUserEmail_success() throws Exception {
//        mockMvc.perform(put("/api/admin/users/123/email")
//                        .with(csrf())
//                        .param("newEmail", "alice@new.com"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Benutzerdetails erfolgreich aktualisiert!"));
//
//        verify(userService).updateUserEmailByAdmin("123", "alice@new.com");
//    }
//
//    @Test
//    @DisplayName("PUT /api/admin/users/{id}/email - Benutzer nicht gefunden (400)")
//    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
//    void updateUserEmail_userNotFound() throws Exception {
//        doThrow(new IllegalArgumentException("Benutzer nicht gefunden!"))
//                .when(userService).updateUserEmailByAdmin("999", "bob@new.com");
//
//        mockMvc.perform(put("/api/admin/users/999/email")
//                        .with(csrf())
//                        .param("newEmail", "bob@new.com"))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("Benutzer nicht gefunden!"));
//    }
//
//    @Test
//    @DisplayName("DELETE /api/admin/users/{id} - Erfolgreiche Löschung (200 OK)")
//    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
//    void deleteUser_success() throws Exception {
//        mockMvc.perform(delete("/api/admin/users/123")
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Benutzerkonto erfolgreich gelöscht!"));
//
//        verify(userService).deleteUserById("123");
//    }
//
//    @Test
//    @DisplayName("DELETE /api/admin/users/{id} - Benutzer nicht gefunden (400)")
//    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
//    void deleteUser_notFound() throws Exception {
//        doThrow(new IllegalArgumentException("Benutzer nicht gefunden!"))
//                .when(userService).deleteUserById("999");
//
//        mockMvc.perform(delete("/api/admin/users/999")
//                        .with(csrf()))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("Fehler beim Löschen: Benutzer nicht gefunden!"));
//    }
//
//    @Test
//    @DisplayName("DELETE /api/admin/users/{id} - Kein Admin (403 Forbidden)")
//    void deleteUser_forbidden() throws Exception {
//        mockMvc.perform(delete("/api/admin/users/123"))
//                .andExpect(status().isForbidden());
//
//        verify(userService, never()).deleteUserById("123");
//    }
//}
