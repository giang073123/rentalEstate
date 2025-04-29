package com.giang.rentalEstate.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giang.rentalEstate.dto.ErrorResponse;
import com.giang.rentalEstate.payload.response.ExceptionResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GeneralExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Resource Not Found Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(UserAlreadyVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyVerifiedException(UserAlreadyVerifiedException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("User Already Verified Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.CONFLICT.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(RentalRequestLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRentalRequestLimitExceededException(RentalRequestLimitExceededException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Rental Request Limit Exceeded Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.CONFLICT.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(DuplicateRentalRequestException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateRentalRequestException(DuplicateRentalRequestException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Duplicate Rental Request Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.CONFLICT.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Data Integrity Violation Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.CONFLICT.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Invalid Token Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(RentalRequestInvalidStatusException.class)
    public ResponseEntity<ErrorResponse> handleRentalRequestInvalidStatusException(RentalRequestInvalidStatusException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Rental Request Invalid Status Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(ResendLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleResendLimitExceededException(ResendLimitExceededException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Resend Limit Exceeded Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }
    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotVerifiedException(UserNotVerifiedException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("User Not Verified Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.FORBIDDEN.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    @ExceptionHandler(AdminDeletionNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleAdminDeletionNotAllowedException(AdminDeletionNotAllowedException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Admin Deletion Not Allowed Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.FORBIDDEN.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    @ExceptionHandler(RentalRequestNotAuthorizedException.class)
    public ResponseEntity<ErrorResponse> handleRentalRequestNotAuthorizedException(RentalRequestNotAuthorizedException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Rental Request Not Authorized Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.FORBIDDEN.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    @ExceptionHandler(NotificationNotAuthorizedException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotAuthorizedException(NotificationNotAuthorizedException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Notification Not Authorized Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.FORBIDDEN.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Access Denied Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.FORBIDDEN.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Bad Credentials Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.UNAUTHORIZED.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorResponse> handleJsonProcessingException(JsonProcessingException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Json Processing Exception Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(PropertyAlreadySavedException.class)
    public ResponseEntity<ErrorResponse> handlePropertyAlreadySavedException(PropertyAlreadySavedException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Property Already Saved Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.CONFLICT.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(PropertyAlreadyReviewedException.class)
    public ResponseEntity<ErrorResponse> handlePropertyAlreadyReviewedException(PropertyAlreadyReviewedException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setError("Property Already Reviewed Error");
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.CONFLICT.value());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", new Date());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", ex.getMessage());
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
