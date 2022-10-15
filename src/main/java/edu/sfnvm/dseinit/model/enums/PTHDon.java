package edu.sfnvm.dseinit.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum PTHDon {
  TVAN((byte) 1), DNKNTT((byte) 2), PORTAL((byte) 3);

  private final byte value;

  PTHDon(byte value) {
    this.value = value;
  }

  @JsonValue
  public byte getValue() {
    return this.value;
  }

  @JsonCreator
  public static PTHDon fromValue(byte value) {
    return Stream.of(PTHDon.values())
      .filter(targetEnum -> targetEnum.value == value)
      .findFirst()
      .orElse(null);
  }
}
