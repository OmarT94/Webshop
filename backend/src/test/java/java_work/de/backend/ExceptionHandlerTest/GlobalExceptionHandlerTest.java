package java_work.de.backend.ExceptionHandlerTest;

import java_work.de.backend.contoller.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.NoSuchElementException;


import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.springframework.test.web.servlet.MockMvc;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    //handleValidationErrors_shouldReturnBadRequest()	        Simuliert eine DTO-Validierung und erwartet 400 BAD REQUEST.
    //handleNoSuchElementException_shouldReturnNotFound()	    Simuliert einen nicht gefundenen Datensatz und erwartet 404 NOT FOUND.
    //handleHttpMessageNotReadable_shouldReturnBadRequest()	    Testet falsches JSON-Format mit 400 BAD REQUEST.
    //handleAllExceptions_shouldReturnInternalServerError() 	Testet einen generischen Fehler mit 500 INTERNAL SERVER ERROR.


    private MockMvc mockMvc;

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(exceptionHandler).build();
    }

    @Test
    void handleValidationErrors_shouldReturnBadRequest() {
        // Mock für BindingResult erstellen
        BindingResult bindingResult = mock(BindingResult.class);

        // Erstelle eine simulierte FieldError-Liste
        List<FieldError> fieldErrors = List.of(new FieldError("userDTO", "email", "E-Mail ist ungültig"));
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Erstelle ein Mock für MethodArgumentNotValidException und übergebe das BindingResult
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // Führe die Methode aus
        var response = exceptionHandler.handleValidationErrors(ex);

        // Überprüfe die Antwort
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("email"));
        assertEquals("E-Mail ist ungültig", response.getBody().get("email"));
    }


    @Test
    void handleNoSuchElementException_shouldReturnNotFound() {
        NoSuchElementException ex = new NoSuchElementException("Objekt nicht gefunden!");

        var response = exceptionHandler.handleNoSuchElementException(ex);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Fehler: Objekt wurde nicht gefunden!", response.getBody());
    }

    @Test
    void handleHttpMessageNotReadable_shouldReturnBadRequest() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Ungültiges JSON");

        var response = exceptionHandler.handleHttpMessageNotReadable(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Fehler: Ungültige JSON-Daten!", response.getBody());
    }

    @Test
    void handleAllExceptions_shouldReturnInternalServerError() {
        Exception ex = new Exception("Testfehler");

        var response = exceptionHandler.handleAllExceptions(ex);

        assertEquals(500, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("500", response.getBody().get("status"));
        assertTrue(response.getBody().get("message").contains("Testfehler"));
        assertNotNull(response.getBody().get("timestamp"));
    }
}
