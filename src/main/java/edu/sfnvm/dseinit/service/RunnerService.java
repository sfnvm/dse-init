package edu.sfnvm.dseinit.service;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.google.common.io.Resources;
import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.dto.enums.SaveType;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.service.io.CacheIoService;
import edu.sfnvm.dseinit.service.io.TbktdLieuMgrIoService;
import edu.sfnvm.dseinit.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RunnerService implements ApplicationRunner {
    private static final String PREPEND_TARGET_QUERY = String.format(
        "SELECT * FROM %s.%s ",
        TbktdLieuMgr.class.getAnnotation(Entity.class).defaultKeyspace(),
        TbktdLieuMgr.class.getAnnotation(CqlName.class).value());
    private static final String SELECT_TBKTDL_BY_PARTITION =
        PREPEND_TARGET_QUERY + "WHERE mst = '%s' AND ntao = '%s'";

    /**
     * <h2>IO Services</h2>
     */
    private final TbktdLieuMgrIoService tbktdLieuMgrIoService;

    private final RetryService retryService;
    private final CacheIoService cacheIoService;

    @Value("${scan.path:}")
    private String scanPath;

    @Value("${retry.till.dead:false}")
    private boolean retryTillDead;

    @Autowired
    public RunnerService(
        TbktdLieuMgrIoService tbktdLieuMgrIoService,
        RetryService retryService,
        CacheIoService cacheIoService) {
        this.tbktdLieuMgrIoService = tbktdLieuMgrIoService;
        this.retryService = retryService;
        this.cacheIoService = cacheIoService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        URL path = StringUtils.hasLength(scanPath)
            ? new URL(scanPath)
            : getClass().getResource("/static/conditions");

        log.info("Start scan file path: {}", path);

        List<Pair<String, Instant>> conditions = Arrays.stream(Resources.toString(
                Objects.requireNonNull(path),
                StandardCharsets.UTF_8).split("\n"))
            .filter(s -> StringUtils.hasLength(s) && !s.startsWith("#"))
            .map(s -> s.split(";"))
            .filter(strings -> strings.length == 2)
            .map(split -> new Pair<>(split[0], DateUtil.parseStringToUtcInstant(split[1])))
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(conditions)) {
            log.info("Condition list empty, skip migrate tbktdlieu");
            return;
        }

        Instant start = Instant.now();
        log.info("Mannual audit data started at: {}", start);

        // Query with ProgressBar status
        try (ProgressBar pb = new ProgressBar("Conditions Step", conditions.size())) {
            conditions.forEach(c -> {
                String query = String.format(SELECT_TBKTDL_BY_PARTITION, c.getValue0(), c.getValue1());
                log.info("=== Start with query: {}", query);
                migrate(query, SaveType.PREPARED);
                pb.step();
            });
        }

        log.info("Start auto retry once last time");
        retryService.retryCached(SaveType.PREPARED);

        if (retryTillDead) {
            retryTillDead();
        }

        // Mark done
        log.info("Mannual audit data done at: {}", Duration.between(start, Instant.now()));
    }

    @SuppressWarnings("SameParameterValue")
    private void migrate(String query, SaveType saveType) {
        final int[] increment = {1}; // Start from 1
        PagingData<TbktdLieuMgr> queryResult = tbktdLieuMgrIoService
            .findWithoutSolrPaging(query, null, 1000, increment[0]);
        while (queryResult.getState() != null) {
            tbktdLieuMgrIoService.loopSave(queryResult.getData(), increment, saveType);
            queryResult = tbktdLieuMgrIoService
                .findWithoutSolrPaging(query, queryResult.getState(), 1000, increment[0]);
            log.info("Complete migrate data with state: {}", queryResult.getState());
            log.info("Current increment: {}", increment[0]);
        }
        // Last page
        tbktdLieuMgrIoService.loopSave(queryResult.getData(), increment, saveType);
        log.info("Complete migrate data with state: {}", queryResult.getState());
        log.info("Current increment: {}", increment[0]);
    }

    private void retryTillDead() {
        log.info("Retry till dead !!!");
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        while (!CollectionUtils.isEmpty(cacheIoService.getStateTimeoutCache())
            && !CollectionUtils.isEmpty(cacheIoService.getMgrTimeoutCache())) {
            executor.scheduleAtFixedRate(() -> {
                log.warn("'Retry till dead' status: Still trying");
                log.warn("'Retry till dead' cacheIoService.getStateTimeoutCache().size(): {}",
                    cacheIoService.getStateTimeoutCache().size());
                log.warn("'Retry till dead' cacheIoService.getMgrTimeoutCache().size(): {}",
                    cacheIoService.getMgrTimeoutCache().size());
                retryService.retryCached(SaveType.SIMPLE);
            }, 0, 5000, TimeUnit.MILLISECONDS);
        }
        executor.shutdown();
    }
}
