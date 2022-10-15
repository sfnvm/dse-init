package edu.sfnvm.dseinit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class EntityValidationException extends Exception {
  private Object details;

  public EntityValidationException(String message) {
    super(message);
  }

  public EntityValidationException(String message, Object details) {
    super(message);
    this.details = details;
  }

  public Object getDetails() {
    return details;
  }
}
