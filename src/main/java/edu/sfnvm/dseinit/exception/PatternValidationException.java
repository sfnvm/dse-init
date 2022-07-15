package edu.sfnvm.dseinit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class PatternValidationException extends Exception {
    public PatternValidationException(String message) {
        super(message);
    }
}
