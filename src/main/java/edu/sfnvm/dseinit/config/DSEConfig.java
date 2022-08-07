package edu.sfnvm.dseinit.config;

import com.datastax.oss.driver.api.core.CqlSession;
import edu.sfnvm.dseinit.codec.PTHDonCodec;
import edu.sfnvm.dseinit.codec.THKTDLieuCodec;
import edu.sfnvm.dseinit.codec.TTTBKTDLieuCodec;
import edu.sfnvm.dseinit.codec.TTXLKTDLieuCodec;
import edu.sfnvm.dseinit.repository.inventory.InventoryMapper;
import edu.sfnvm.dseinit.repository.inventory.InventoryMapperBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DSEConfig {
  @Value("#{'${spring.data.cassandra.contact-points}'.split(',')}")
  private List<String> hosts;

  @Value("${spring.data.cassandra.local-datacenter}")
  private String localDatacenter;

  @Value("${spring.data.cassandra.username}")
  private String username;

  @Value("${spring.data.cassandra.password}")
  private String password;

  @Bean
  public CqlSession cqlSession() {
    List<InetSocketAddress> contactPoints = hosts.parallelStream()
      .map(host -> host.split(":"))
      .map(host -> new InetSocketAddress(host[0], Integer.parseInt(host[1])))
      .collect(Collectors.toList());

    return CqlSession.builder()
      .addContactPoints(contactPoints)
      .withLocalDatacenter(localDatacenter)
      .withAuthCredentials(username, password)
      .addTypeCodecs(
        new PTHDonCodec(),
        new THKTDLieuCodec(),
        new TTTBKTDLieuCodec(),
        new TTXLKTDLieuCodec()
      )
      .build();
  }

  @Bean
  public InventoryMapper inventoryMapper(@Autowired CqlSession cqlSession) {
    return new InventoryMapperBuilder(cqlSession).build();
  }

  // @Bean
  // public DummyInventoryMapper dummyInventoryMapper(@Autowired CqlSession cqlSession) {
  // 	return new DummyInventoryMapperBuilder(cqlSession).build();
  // }
}
