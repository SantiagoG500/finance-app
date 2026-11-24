package com.finance.transaction.infraestructure.entry_points.exception;

import com.finance.transaction.infraestructure.entry_points.dto.ErrorResponseDTO;
import feign.FeignException;
import feign.RetryableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponseDTO> handleSecurityException(
            SecurityException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, validationErrors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String validationErrors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, validationErrors, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud JSON está malformado o es inválido",
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String message = String.format(
                "El parámetro '%s' tiene un formato inválido",
                ex.getName()
        );
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        String message = String.format(
                "El parámetro requerido '%s' no fue proporcionado",
                ex.getParameterName()
        );
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoSuchElementException(
            NoSuchElementException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflicto de integridad de datos en la base de datos",
                request
        );
    }

    @ExceptionHandler({TimeoutException.class, RetryableException.class})
    public ResponseEntity<ErrorResponseDTO> handleTimeoutException(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.GATEWAY_TIMEOUT,
                "Tiempo de espera agotado al comunicarse con un servicio externo",
                request
        );
    }

    @ExceptionHandler(TaskRejectedException.class)
    public ResponseEntity<ErrorResponseDTO> handleTaskRejectedException(
            TaskRejectedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "El servidor está saturado temporalmente, intente nuevamente",
                request
        );
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorResponseDTO> handleFeignNotFoundException(
            FeignException.NotFound ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Moneda no soportada por el proveedor externo",
                request
        );
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponseDTO> handleFeignException(
            FeignException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                "Proveedor externo de divisas temporalmente no disponible",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno inesperado en el servidor",
                request
        );
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(Instant.now().toString())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}
