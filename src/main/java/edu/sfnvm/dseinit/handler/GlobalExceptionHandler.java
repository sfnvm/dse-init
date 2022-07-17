package edu.sfnvm.dseinit.handler;

import com.datastax.oss.driver.api.core.servererrors.QueryValidationException;
import edu.sfnvm.dseinit.dto.ErrorDetail;
import edu.sfnvm.dseinit.exception.EntityValidationException;
import edu.sfnvm.dseinit.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    @Autowired
    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetail> resourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(
                Instant.now(),
                ex.getMessage(),
                "",
                request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityValidationException.class)
    public ResponseEntity<ErrorDetail> entityValidationException(
            EntityValidationException ex,
            WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(
                Instant.now(),
                ex.getMessage(),
                ex.getDetails(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(QueryValidationException.class)
    public ResponseEntity<ErrorDetail> queryValidationException(
            QueryValidationException ex,
            WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(
                Instant.now(),
                ex.getMessage(),
                "",
                request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetail> illegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(
                Instant.now(),
                ex.getMessage(),
                "",
                request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetail> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(
                Instant.now(),
                ex.getMessage(),
                "",
                request.getDescription(false));
        Map<String, String> errors = new HashMap<>();
        Class clazz = ex.getBindingResult().getTarget().getClass();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            errors.put(((FieldError) error).getField(), error.getDefaultMessage());
        });

        if (!CollectionUtils.isEmpty(errors)) {
            errorDetail.setMessage(messageSource.getMessage(
                    "error.validation",
                    new Object[0],
                    LocaleContextHolder.getLocale()));
            errorDetail.setDetails(errors);
        }

        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetail> globalExceptionHandler(Exception ex, WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(
                Instant.now(),
                ex.getMessage(),
                "",
                request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
