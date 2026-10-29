package com.utegear.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> handleApiException(ApiException exception, HttpServletRequest request) {
        ApiError error = error(
                exception.getStatus(), exception.getCode(), exception.getMessage(), request, Map.of());
        return ResponseEntity.status(exception.getStatus()).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleDataIntegrity(
            DataIntegrityViolationException exception, HttpServletRequest request) {
        log.warn("Data integrity violation at {}: {}", request.getRequestURI(), exception.getMessage());
        ApiError error = error(
                HttpStatus.CONFLICT,
                "DATA_CONFLICT",
                "Dữ liệu đã tồn tại hoặc vi phạm ràng buộc hệ thống",
                request,
                Map.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(CannotCreateTransactionException.class)
    ResponseEntity<ApiError> handleDbUnavailable(
            CannotCreateTransactionException exception, HttpServletRequest request) {
        log.error("Database connection unavailable at {}: {}", request.getRequestURI(), exception.getMessage());
        ApiError error = error(
                HttpStatus.SERVICE_UNAVAILABLE,
                "DATABASE_BUSY",
                "Máy chủ cơ sở dữ liệu đang bận, vui lòng thử lại sau vài giây",
                request,
                Map.of());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));
        String message = fieldErrors.isEmpty()
                ? "Dữ liệu không hợp lệ"
                : String.join("; ", fieldErrors.values());
        ApiError error = error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                message,
                request,
                fieldErrors);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiError> handleMissingParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {
        ApiError error = error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Thiếu tham số bắt buộc: " + exception.getParameterName(),
                request,
                Map.of(exception.getParameterName(), "Trường này là bắt buộc"));
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> handleMaxUploadSize(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request) {
        ApiError error = error(
                HttpStatus.BAD_REQUEST,
                "FILE_TOO_LARGE",
                "File ảnh vượt quá dung lượng cho phép",
                request,
                Map.of());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleGeneralException(
            Exception exception,
            HttpServletRequest request) {
        log.error("Unhandled exception at " + request.getRequestURI(), exception);
        String message = exception.getMessage() != null && !exception.getMessage().isBlank()
                ? exception.getMessage()
                : "Đã xảy ra sự cố máy chủ, vui lòng thử lại sau";
        ApiError error = error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                message,
                request,
                Map.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private ApiError error(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI(), fieldErrors);
    }
}
