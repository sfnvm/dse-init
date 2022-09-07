package edu.sfnvm.dseinit;

import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.service.io.TbktdLieuMgrIoService;
import edu.sfnvm.dseinit.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
class DummyBatchInsertTest {
    private final TbktdLieuMgrIoService tbktdLieuMgrIoService;

    @Autowired
    public DummyBatchInsertTest(TbktdLieuMgrIoService tbktdLieuMgrIoService) {
        this.tbktdLieuMgrIoService = tbktdLieuMgrIoService;
    }

    @Test
    void bulkInsertTest() {
        EasyRandom er = new EasyRandom();
        List<TbktdLieuMgr> toInsert = new ArrayList<>();

        final String mst = "4100258955";
        final Instant ins = DateUtil.parseStringToUtcInstant("2022-06-28T00:00:00.000Z");

        IntStream.range(0, 20000).forEach(value -> {
            TbktdLieuMgr tmp = er.nextObject(TbktdLieuMgr.class);
            tmp.setMst(mst);
            tmp.setNtao(ins);
            tmp.setId(UUID.randomUUID());

            tmp.setTtctiet(new ArrayList<>());
            tmp.setDsloi(new ArrayList<>());

            toInsert.add(tmp);
        });

        log.info("Done init input");

        List<BatchableStatement<?>> batch = new ArrayList<>();
        toInsert.forEach(e -> batch.add(tbktdLieuMgrIoService.boundStatementSave(e)));

        log.info("Done init batch");

        tbktdLieuMgrIoService.executeBatch(batch, BatchType.UNLOGGED, 100);
    }
}
