package edu.sfnvm.dseinit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ErrorDetail {
  private Instant timestamp;
  private String message;
  private Object details;
  private String path;
}