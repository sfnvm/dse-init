package edu.sfnvm.dseinit.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum TTTBKTDLieu {
  NOT_SEND((byte) 0),
  SENT_SUCCESS((byte) 1),
  SENT_FAILURE((byte) 2);

  private final byte value;

  TTTBKTDLieu(byte value) {
    this.value = value;
  }

  @JsonValue
  public byte getValue() {
    return value;
  }

  @JsonCreator
  public static TTTBKTDLieu fromValue(final byte value) {
    return Stream.of(TTTBKTDLieu.values())
      .filter(targetEnum -> targetEnum.value == value)
      .findFirst()
      .orElse(null);
  }
}
