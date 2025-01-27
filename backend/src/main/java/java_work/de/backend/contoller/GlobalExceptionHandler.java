package java_work.de.backend.contoller;
import java_work.de.backend.model.ErrorMessage;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.NoSuchElementException;

@RestControllerAdvice
@ResponseStatus
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ErrorMessage  handleNoSuchElementException(NoSuchElementException ex) {
        return new ErrorMessage(ex.getMessage(), Instant.now());
    }

}
