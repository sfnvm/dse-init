package edu.sfnvm.dseinit.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum TTXLKTDLieu {
  NOT_SIGN((byte) 0),
  SIGN_SUCCESS((byte) 1),
  SIGN_FAILURE((byte) 2);

  private final byte value;

  TTXLKTDLieu(byte value) {
    this.value = value;
  }

  @JsonValue
  public byte getValue() {
    return this.value;
  }

  @JsonCreator
  public static TTXLKTDLieu fromValue(byte value) {
    return Stream.of(TTXLKTDLieu.values())
      .filter(targetEnum -> targetEnum.value == value)
      .findFirst()
      .orElse(null);
  }
}