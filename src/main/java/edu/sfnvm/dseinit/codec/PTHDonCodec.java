package edu.sfnvm.dseinit.codec;

import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import edu.sfnvm.dseinit.model.enums.PTHDon;
import edu.umd.cs.findbugs.annotations.Nullable;

public class PTHDonCodec extends MappingCodec<Byte, PTHDon> {
    public PTHDonCodec() {
        super(TypeCodecs.TINYINT, GenericType.of(PTHDon.class));
    }

    @Nullable
    @Override
    protected PTHDon innerToOuter(@Nullable Byte value) {
        return value == null ? null : PTHDon.fromValue(value);
    }

    @Nullable
    @Override
    protected Byte outerToInner(@Nullable PTHDon value) {
        return value == null ? null : value.getValue();
    }
}