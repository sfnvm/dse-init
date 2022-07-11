package edu.sfnvm.dseinit.codec;

import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import edu.sfnvm.dseinit.model.enums.TTXLKTDLieu;
import edu.umd.cs.findbugs.annotations.Nullable;

public class TTXLKTDLieuCodec extends MappingCodec<Byte, TTXLKTDLieu> {
    public TTXLKTDLieuCodec() {
        super(TypeCodecs.TINYINT, GenericType.of(TTXLKTDLieu.class));
    }

    @Nullable
    @Override
    protected TTXLKTDLieu innerToOuter(@Nullable Byte value) {
        return value == null ? null : TTXLKTDLieu.fromValue(value);
    }

    @Nullable
    @Override
    protected Byte outerToInner(@Nullable TTXLKTDLieu value) {
        return value == null ? null : value.getValue();
    }
}