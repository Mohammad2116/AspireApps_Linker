package ir.aspireapps.linker.userservice.error;

import ir.aspireapps.linker.common.dto.ApiExceptionInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    @ExceptionHandler(GeneralBusinessException.class)
    public ResponseEntity<ApiExceptionInfo> handleGeneralBusinessException(
            GeneralBusinessException ex,
            HttpServletRequest request) {
        ApiExceptionInfo info = ApiExceptionInfo.builder()
                .timestamp(Instant.now())
                .code(ex.getCode())
                .status(ex.getHttpStatus().value())
                .message(ex.getMessage())
                .error(ex.getHttpStatus().getReasonPhrase())
                .path(request.getRequestURI())
                .errors(null)
                .build();
        return ResponseEntity
                .status(info.status())
                .body(info);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiExceptionInfo> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                (error) -> {
                    errors.put(error.getField(), error.getDefaultMessage());
                }
        );
        ApiExceptionInfo info = ApiExceptionInfo.builder()
                .timestamp(Instant.now())
                .code("INVALIDATION_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .path(request.getRequestURI())
                .errors(errors)
                .build();
        return ResponseEntity
                .status(info.status())
                .body(info);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiExceptionInfo> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {
        ApiExceptionInfo info = ApiExceptionInfo.builder()
                .timestamp(Instant.now())
                .code("UNAUTHORIZED_ERROR")
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid username or password")
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .path(request.getRequestURI())
                .errors(null)
                .build();
        return ResponseEntity
                .status(info.status())
                .body(info);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiExceptionInfo> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        ApiExceptionInfo info = ApiExceptionInfo.builder()
                .timestamp(Instant.now())
                .code("INVALID_REQUEST_BODY")
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Request body is invalid")
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .path(request.getRequestURI())
                .errors(null)
                .build();
        return ResponseEntity
                .status(info.status())
                .body(info);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiExceptionInfo> handleException(
            Exception ex,
            HttpServletRequest request) {
        log.error(
                "Unexpected exception: method={}, path={}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );
        ApiExceptionInfo info = ApiExceptionInfo.builder()
                .timestamp(Instant.now())
                .code("INTERNAL_SERVER_ERROR")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Something went wrong")
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .path(request.getRequestURI())
                .errors(null)
                .build();
        return ResponseEntity
                .status(info.status())
                .body(info);
    }
}