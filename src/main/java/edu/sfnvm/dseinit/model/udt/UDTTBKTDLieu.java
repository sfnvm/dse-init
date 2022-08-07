package edu.sfnvm.dseinit.model.udt;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.NamingStrategy;
import com.datastax.oss.driver.api.mapper.entity.naming.NamingConvention;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Entity(defaultKeyspace = "ks_hoadon")
@CqlName("udt_tbktdl")
@NamingStrategy(convention = NamingConvention.CASE_INSENSITIVE)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UDTTBKTDLieu {
  private Byte ltbao;
  private Byte loai;
  private Byte khmshdon;
  private String khhdon;
  private Integer shdon;
  private Instant tdlap;
  private String tnmua;
  private String kdlieu;
  private Byte ldau;
  private Integer bslthu;
  private Integer sbthdlieu;
  private String mhhoa;
  private String thhdvu;
  private List<UDTLoi> dsloi;
}
