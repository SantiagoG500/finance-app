package com.finance.account.infraestructure.entry_points.exception;

import feign.RetryableException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Validation / Business rule violation on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage(), request);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNoSuchElementException(NoSuchElementException ex, HttpServletRequest request) {
        log.warn("Resource not found on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
        log.warn("Conflict on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflicto de estado", ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflicto de integridad de datos", "Violación de restricción de integridad en la base de datos", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Bean validation failed on [{}]: {}", request.getRequestURI(), details);
        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación de campos", details, request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
        String message = String.format("El encabezado HTTP '%s' es obligatorio", ex.getHeaderName());
        return buildResponse(HttpStatus.BAD_REQUEST, "Encabezado faltante", message, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("El parámetro '%s' de tipo '%s' es obligatorio", ex.getParameterName(), ex.getParameterType());
        return buildResponse(HttpStatus.BAD_REQUEST, "Parámetro faltante", message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido";
        String message = String.format("El parámetro '%s' con valor '%s' no tiene un formato válido (se esperaba %s)",
                ex.getName(), ex.getValue(), expectedType);
        return buildResponse(HttpStatus.BAD_REQUEST, "Formato de parámetro inválido", message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Cuerpo JSON inválido o malformado",
                "Verifique la sintaxis del JSON enviado y los tipos de datos de cada campo", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String message = String.format("El método HTTP '%s' no está soportado para esta ruta", ex.getMethod());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido", message, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Ruta no encontrada",
                "No existe ningún endpoint registrado en: " + request.getRequestURI(), request);
    }

    @ExceptionHandler({TimeoutException.class, RetryableException.class})
    public ResponseEntity<Map<String, Object>> handleTimeoutException(Exception ex, HttpServletRequest request) {
        log.warn("External service timeout on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.GATEWAY_TIMEOUT, "Tiempo de espera agotado",
                "Tiempo de espera agotado al comunicarse con un servicio externo", request);
    }

    @ExceptionHandler(TaskRejectedException.class)
    public ResponseEntity<Map<String, Object>> handleTaskRejectedException(TaskRejectedException ex, HttpServletRequest request) {
        log.warn("Task rejected due to thread pool saturation on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Servicio no disponible",
                "El servidor está saturado temporalmente, intente nuevamente", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected internal error on [{} {}]: ", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor",
                "Ocurrió un error inesperado al procesar la solicitud", request);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
