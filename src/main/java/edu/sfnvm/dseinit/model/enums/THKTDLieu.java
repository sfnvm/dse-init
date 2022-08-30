package edu.sfnvm.dseinit.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum THKTDLieu {
    SEMI_VALID((byte) 1),
    SEMI_INVALID((byte) 2),
    CODE_UNCONDITIONAL((byte) 3),
    NOCODE_UNCONDITIONAL((byte) 4),
    AGGREGATION_UNCONDITIONAL((byte) 5);

    private final byte value;

    THKTDLieu(byte value) {
        this.value = value;
    }

    @JsonValue
    public byte getValue() {
        return value;
    }

    @JsonCreator
    public static THKTDLieu fromValue(final byte value) {
        return Stream.of(THKTDLieu.values())
            .filter(targetEnum -> targetEnum.value == value)
            .findFirst()
            .orElse(null);
    }
}
