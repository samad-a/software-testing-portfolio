package ilp.samad.ilpcoursework1.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String GENERIC_ERROR_MESSAGE = "Bad Request";

    // as per instructor advice to not expose internal information to user, error messages belong in a log
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.warn("Validation Error: {}", ex.getMessage());
        return new ResponseEntity<>(GENERIC_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Semantic Error: {}", ex.getMessage());
        return new ResponseEntity<>(GENERIC_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleParsingErrors(HttpMessageNotReadableException ex) {
        logger.warn("JSON Parsing Error: {}", ex.getMessage());
        return new ResponseEntity<>(GENERIC_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllOtherExceptions(Exception ex) {
        logger.error("Unexpected Error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(GENERIC_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    // specifically for 2b 404 error case
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        logger.warn("Status Error: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
    }
}
