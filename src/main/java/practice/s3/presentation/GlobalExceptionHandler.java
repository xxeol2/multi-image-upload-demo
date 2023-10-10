package practice.s3.presentation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import practice.s3.dto.ErrorResponse;
import practice.s3.exception.ImageStorageException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ImageStorageException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        log.warn(e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception e) {
        log.error("[InternalServerError]", e);
        return ResponseEntity.internalServerError().body(new ErrorResponse("Internal Server Error."));
    }
}
