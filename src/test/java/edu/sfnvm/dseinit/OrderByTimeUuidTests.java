package edu.sfnvm.dseinit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * <a href="https://www.baeldung.com/java-uuid">Guide to UUID in Java</a>
 */
@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderByTimeUuidTests {
  // private final DummyKaiRepository dummyKaiRepository;
  //
  // @Autowired
  // public OrderByTimeUuidTests(DummyInventoryMapper dummyInventoryMapper) {
  // 	this.dummyKaiRepository = dummyInventoryMapper.dummyKaiRepository();
  // }
  //
  // @Test
  // @Order(1)
  // void insert() {
  // 	final String pk = "1000";
  // 	dummyKaiRepository.deleteByPartition(pk);
  // 	IntStream.range(0, 1000).forEach(i ->
  // 			dummyKaiRepository.saveSync(DummyKai.builder()
  // 					.someUniqueValue(pk)
  // 					.timeUuid(Uuids.timeBased())
  // 					.createdTimestamp(Instant.ofEpochSecond(Instant.now().getEpochSecond() + i))
  // 					.build()));
  // }
  //
  // @Test
  // @Order(1)
  // void select() {
  // 	List<DummyKai> rs = dummyKaiRepository.selectAscByTimeUuid("1000").all();
  // 	for (DummyKai r : rs) {
  // 		log.info(r.toString());
  // 	}
  // }
}
