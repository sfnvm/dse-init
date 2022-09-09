package edu.sfnvm.dseinit;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.model.udt.UDTLoi;
import edu.sfnvm.dseinit.model.udt.UDTTBKTDLieu;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class DummyTests {
    @Test
    void collisionUuidV1Test() throws InterruptedException {
        Map<UUID, UUID> generated = new ConcurrentHashMap<>();
        final int numbConcurrent = 1000;
        ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.newFixedThreadPool(numbConcurrent);
        for (int i = 0; i < numbConcurrent; i++) {
            es.submit(() -> {
                for (int j = 0; j < 1000000 / numbConcurrent; j++) {
                    UUID tmp = Uuids.timeBased();
                    log.info("key to add: {}", tmp);
                    if (generated.containsKey(tmp)) {
                        log.info("duplicated key: {}", tmp);
                    }
                    generated.put(tmp, tmp);
                }
            });
        }

        Map<UUID, UUID> syncGenerated = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                UUID tmp = Uuids.timeBased();
                syncGenerated.put(tmp, tmp);
            }
        }

        Thread.sleep(10000);

        log.info("size of result run async: {}", generated.keySet().size());
        log.info("size of result run sync: {}", syncGenerated.keySet().size());
    }

    @Test
    void streamAnyMatchTest() {
        List<String> l01 = Arrays.asList(null, "2", "3");
        List<String> l02 = Arrays.asList("", "", "");
        List<String> l03 = Arrays.asList("1", "", "");
        List<String> l04 = new ArrayList<>();
        log.info(String.valueOf(l01.stream().anyMatch(StringUtils::isEmpty)));
        log.info(String.valueOf(l02.stream().anyMatch(StringUtils::isEmpty)));
        log.info(String.valueOf(l03.stream().anyMatch(StringUtils::isEmpty)));
        log.info(String.valueOf(l04.stream().anyMatch(StringUtils::isEmpty)));
    }

    @Test
    void udtDsloiGchuNnTest() {
        List<UDTLoi> validUdtLoiArr =
            Arrays.asList(
                UDTLoi.builder().gchu("Not null").build(),
                UDTLoi.builder().build()
            );

        List<UDTLoi> inValidUdtLoiArr =
            Collections.singletonList(UDTLoi.builder().build());

        TbktdLieuMgr item = TbktdLieuMgr.builder()
            .dsloi(new ArrayList<>())
            .ttctiet(Collections.singletonList(UDTTBKTDLieu.builder()
                .dsloi(validUdtLoiArr)
                .build()))
            .build();

        boolean validDsLoi = !CollectionUtils.isEmpty(item.getDsloi())
            && item.getDsloi().stream().anyMatch(u -> !StringUtils.isEmpty(u.getGchu()));

        boolean validTtctiet = !CollectionUtils.isEmpty(item.getTtctiet())
            && item.getTtctiet().stream()
            .anyMatch(
                u -> !CollectionUtils.isEmpty(u.getDsloi())
                    && u.getDsloi().stream().anyMatch(l -> !StringUtils.isEmpty(l.getGchu())));

        log.info("validDsLoi {}", validDsLoi);
        log.info("validTtctiet {}", validTtctiet);
    }
}
