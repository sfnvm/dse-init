package edu.sfnvm.dseinit;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
}
