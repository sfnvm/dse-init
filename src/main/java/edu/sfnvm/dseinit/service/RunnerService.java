package edu.sfnvm.dseinit.service;

import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.google.common.io.Resources;
import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.exception.ResourceNotFoundException;
import edu.sfnvm.dseinit.mapper.TbktdLieuNewMapper;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.model.TbktdLieuNew;
import edu.sfnvm.dseinit.service.io.TbktdLieuMgrIoService;
import edu.sfnvm.dseinit.service.io.TbktdLieuNewIoService;
import edu.sfnvm.dseinit.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class RunnerService implements ApplicationRunner {
    private final TbktdLieuMgrIoService tbktDLieuMgrIoService;
    private final TbktdLieuNewIoService tbktDLieuNewIoService;

    private static final String SELECT_TBKTDL_BY_PARTITION =
            "SELECT * FROM ks_hoadon.tbktdl_mgr WHERE mst = '%s' AND ntao = '%s'";

    private final TbktdLieuNewMapper mapper = Mappers.getMapper(TbktdLieuNewMapper.class);

    @Autowired
    public RunnerService(
            TbktdLieuMgrIoService tbktDLieuMgrIoService,
            TbktdLieuNewIoService tbktDLieuNewIoService) {
        this.tbktDLieuMgrIoService = tbktDLieuMgrIoService;
        this.tbktDLieuNewIoService = tbktDLieuNewIoService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Instant start = Instant.now();
        log.info("Mannual audit data started at: {}", start);

        // Insert mockData
        // bulkInsert();

        // Main migrate
        List<Pair<String, Instant>> conditions = Arrays.stream(Resources.toString(
                        Objects.requireNonNull(getClass().getResource("/static/conditions")),
                        StandardCharsets.UTF_8).split("\n"))
                .filter(StringUtils::hasText)
                .map(s -> {
                    String[] split = s.split(";");
                    return new Pair<>(split[0], DateUtil.parseStringToUtcInstant(split[1]));
                }).collect(Collectors.toList());
        conditions.forEach(c -> {
            String query = String.format(SELECT_TBKTDL_BY_PARTITION, c.getValue0(), c.getValue1());
            log.info("=== Start with query: {}", query);
            migrate(query);
        });

        log.info("Mannual audit data done at: {}", Duration.between(start, Instant.now()));
    }

    private void migrate(String query) {
        PagingData<TbktdLieuMgr> queryResult = tbktDLieuMgrIoService.findWithoutSolrPaging(query, null, 5000);

        final int[] increment = {0};
        while (queryResult.getState() != null) {
            loopSave(queryResult, increment);
            queryResult = tbktDLieuMgrIoService.findWithoutSolrPaging(query, queryResult.getState(), 5000);
        }
        // Get last page of records
        loopSave(queryResult, increment);
    }

    private void loopSave(PagingData<TbktdLieuMgr> queryResult, int[] increment) {
        queryResult.getData().forEach(mgr -> {
            TbktdLieuNew toInsert = builder(mgr, increment[0] % 86399999, ChronoUnit.MILLIS);
            tbktDLieuNewIoService.saveAsync(toInsert);
            increment[0]++;
        });
        log.info("Complete migrate data with state: {}", queryResult.getState());
        log.info("Current increment: {}", increment[0]);
    }

    private void bulkInsert() throws ResourceNotFoundException {
        final String mst = "0102738332";
        final Instant ins = DateUtil.parseStringToUtcInstant("2022-05-11T00:00:00.000Z");
        final UUID id = UUID.fromString("79075673-199b-41e8-80fa-445a3848087a");

        TbktdLieuMgr sampleModel = tbktDLieuMgrIoService.findByPartitionKeys(mst, ins, id);

        List<BatchableStatement<?>> batch = new ArrayList<>();
        IntStream.range(0, 1000000).forEach(i -> {
            sampleModel.setId(UUID.randomUUID());
            batch.add(tbktDLieuMgrIoService.boundStatementSave(sampleModel));
        });

        tbktDLieuMgrIoService.executeBatch(batch, BatchType.UNLOGGED, 200);
    }

    private TbktdLieuNew builder(TbktdLieuMgr sourceData, long incrementValue, ChronoUnit unitType) {
        TbktdLieuNew result = mapper.map(sourceData);
        result.setNtao(sourceData.getNtao().plus(incrementValue, unitType));
        return result;
    }
}
