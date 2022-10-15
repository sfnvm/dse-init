package edu.sfnvm.dseinit.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StateTimeoutDto {
  private String query;
  private String state;
  private int increment;
  private int querySize;
}
