package edu.sfnvm.dseinit.model;

import com.datastax.oss.driver.api.mapper.annotations.*;
import com.datastax.oss.driver.api.mapper.entity.naming.NamingConvention;
import edu.sfnvm.dseinit.model.enums.PTHDon;
import edu.sfnvm.dseinit.model.enums.THKTDLieu;
import edu.sfnvm.dseinit.model.enums.TTTBKTDLieu;
import edu.sfnvm.dseinit.model.enums.TTXLKTDLieu;
import edu.sfnvm.dseinit.model.udt.UDTLoi;
import edu.sfnvm.dseinit.model.udt.UDTTBKTDLieu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(defaultKeyspace = "ks_hoadon")
@NamingStrategy(convention = NamingConvention.CASE_INSENSITIVE)
// @CqlName("hddt_tbktdl_target")
@CqlName("tbktdl_target")
public class TbktdLieuNew {
    @PartitionKey(1)
    private String mst;

    @PartitionKey(2)
    private Instant ntao;

    @ClusteringColumn
    private UUID id;

    private String mso;
    private String ddanh;
    private String ten;
    private String tnnt;
    private Byte ltbao;
    private THKTDLieu thop;
    private String ccu;
    private Instant nnhan;
    private PTHDon ptgui;
    private String tvandnkntt;
    private String mtdtchieu;
    private Integer sluong;
    private String mcqt;
    private String tcqt;
    private List<UDTTBKTDLieu> ttctiet;
    private Instant ncnhat;
    private String pban;
    private TTXLKTDLieu ttxly;
    private String so;
    private Instant ngay;
    private Instant nky;
    private String mlky;
    private String tlky;
    private String cks;
    private UUID hsgoc;
    private TTTBKTDLieu tttbao;
    private Instant ngtbao;
    private String mtdiep;
    private String mlgtbao;
    private String tlgtbao;
    private List<UDTLoi> dsloi;
    private String khhdon;
    private Byte khmshdon;
    private String mdvqhnsach;
    private Integer shdon;
    private Instant tdlap;
    private String tlhdon;
}
