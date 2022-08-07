package edu.sfnvm.dseinit.codec;

import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import edu.sfnvm.dseinit.model.enums.TTTBKTDLieu;
import edu.umd.cs.findbugs.annotations.Nullable;

public class TTTBKTDLieuCodec extends MappingCodec<Byte, TTTBKTDLieu> {
  public TTTBKTDLieuCodec() {
    super(TypeCodecs.TINYINT, GenericType.of(TTTBKTDLieu.class));
  }

  @Nullable
  @Override
  protected TTTBKTDLieu innerToOuter(@Nullable Byte value) {
    return value == null ? null : TTTBKTDLieu.fromValue(value);
  }

  @Nullable
  @Override
  protected Byte outerToInner(@Nullable TTTBKTDLieu value) {
    return value == null ? null : value.getValue();
  }
}
