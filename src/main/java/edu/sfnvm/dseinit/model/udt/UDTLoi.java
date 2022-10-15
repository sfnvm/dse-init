package edu.sfnvm.dseinit.model.udt;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.NamingStrategy;
import com.datastax.oss.driver.api.mapper.entity.naming.NamingConvention;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(defaultKeyspace = "ks_hoadon")
@CqlName("udt_loi")
@NamingStrategy(convention = NamingConvention.CASE_INSENSITIVE)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UDTLoi {
  private String ma;
  private String ten;
  private String hdgquyet;
  private String gchu;
}
