package edu.sfnvm.dseinit.codec;

import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import edu.sfnvm.dseinit.model.enums.THKTDLieu;
import edu.umd.cs.findbugs.annotations.Nullable;

public class THKTDLieuCodec extends MappingCodec<Byte, THKTDLieu> {
  public THKTDLieuCodec() {
    super(TypeCodecs.TINYINT, GenericType.of(THKTDLieu.class));
  }

  @Nullable
  @Override
  protected THKTDLieu innerToOuter(@Nullable Byte value) {
    return value == null ? null : THKTDLieu.fromValue(value);
  }

  @Nullable
  @Override
  protected Byte outerToInner(@Nullable THKTDLieu value) {
    return value == null ? null : value.getValue();
  }
}