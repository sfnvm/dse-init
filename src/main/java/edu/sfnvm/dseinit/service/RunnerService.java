package edu.sfnvm.dseinit.service;

import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.io.Resources;
import edu.sfnvm.dseinit.cache.CacheConstants;
import edu.sfnvm.dseinit.cache.MgrTimeoutCache;
import edu.sfnvm.dseinit.cache.StateTimeoutCache;
import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.dto.StateTimeoutDto;
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
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
    /**
     * <h2>IO Services</h2>
     */
    private final TbktdLieuMgrIoService tbktDLieuMgrIoService;
    private final TbktdLieuNewIoService tbktDLieuNewIoService;

    /**
     * <h2>Cache Management</h2>
     */
    private final CacheManager cacheManager;
    private final MgrTimeoutCache mgrTimeoutCache;
    private final StateTimeoutCache stateTimeoutCache;

    private static final String SELECT_TBKTDL_BY_PARTITION =
            "SELECT * FROM ks_hoadon.tbktdl_mgr WHERE mst = '%s' AND ntao = '%s'";
    private static final long NANOS_WITHIN_A_DAY = 86399999;

    private final TbktdLieuNewMapper mapper = Mappers.getMapper(TbktdLieuNewMapper.class);

    @Autowired
    public RunnerService(
            TbktdLieuMgrIoService tbktDLieuMgrIoService,
            TbktdLieuNewIoService tbktDLieuNewIoService,
            CacheManager cacheManager,
            MgrTimeoutCache mgrTimeoutCache,
            StateTimeoutCache stateTimeoutCache) {
        this.tbktDLieuMgrIoService = tbktDLieuMgrIoService;
        this.tbktDLieuNewIoService = tbktDLieuNewIoService;
        this.cacheManager = cacheManager;
        this.mgrTimeoutCache = mgrTimeoutCache;
        this.stateTimeoutCache = stateTimeoutCache;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Pair<String, Instant>> conditions = Arrays.stream(Resources.toString(
                        Objects.requireNonNull(getClass().getResource("/static/conditions")),
                        StandardCharsets.UTF_8).split("\n"))
                .filter(s -> StringUtils.hasLength(s) && !s.startsWith("#"))
                .map(s -> {
                    String[] split = s.split(";");
                    return new Pair<>(split[0], DateUtil.parseStringToUtcInstant(split[1]));
                }).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(conditions)) {
            Instant start = Instant.now();
            log.info("Mannual audit data started at: {}", start);
            conditions.forEach(c -> {
                String query = String.format(SELECT_TBKTDL_BY_PARTITION, c.getValue0(), c.getValue1());
                log.info("=== Start with query: {}", query);
                migrate(query);
            });
            log.info("Mannual audit data done at: {}", Duration.between(start, Instant.now()));
        }
    }

    public void retryCached() {
        // Retry async failed
        List<TbktdLieuNew> toRetry = getMgrTimeoutCache();
        mgrTimeoutCache.clearCache();
        for (TbktdLieuNew tbktdLieuNew : toRetry) {
            tbktDLieuNewIoService.saveAsync(tbktdLieuNew);
        }
        // Retry paging failed
        List<StateTimeoutDto> stateToRetry = getStateTimeoutCache();
        mgrTimeoutCache.clearCache();
        for (StateTimeoutDto dto : stateToRetry) {
            PagingData<TbktdLieuMgr> queryResult = tbktDLieuMgrIoService.findWithoutSolrPaging(
                    dto.getQuery(),
                    dto.getState(),
                    dto.getQuerySize(),
                    dto.getIncrement());
            loopSave(queryResult.getData(), new int[]{dto.getIncrement()});
        }
    }

    public List<TbktdLieuNew> getMgrTimeoutCache() {
        Cache<Object, Object> nativeCache = getCache(CacheConstants.RETRY);
        List<TbktdLieuNew> cachedList = nativeCache.asMap().values().stream()
                .map(o -> (TbktdLieuNew) o)
                .collect(Collectors.toList());
        log.info("Cache current size {}", cachedList.size());
        return cachedList;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<StateTimeoutDto> getStateTimeoutCache() {
        Cache<Object, Object> nativeCache = getCache(CacheConstants.STATE);
        List cachedList = nativeCache.asMap().values().stream()
                .map(o -> (StateTimeoutDto) o)
                .collect(Collectors.toList());
        log.info("Cache current size {}", cachedList.size());
        return cachedList;
    }

    public TbktdLieuNew putMgrTimeoutCache(TbktdLieuNew tbktdLieuNew) {
        return mgrTimeoutCache.cache(tbktdLieuNew);
    }

    public StateTimeoutDto putStateTimeoutCache(StateTimeoutDto stateTimeoutDto) {
        stateTimeoutCache.cache(stateTimeoutDto);
        return stateTimeoutCache.cache(stateTimeoutDto);
    }

    public void clearMgrTimeoutCache() {
        mgrTimeoutCache.clearCache();
    }

    public void clearStateTimeoutCache() {
        stateTimeoutCache.clearCache();
    }

    private void migrate(String query) {
        final int[] increment = {1};
        PagingData<TbktdLieuMgr> queryResult = tbktDLieuMgrIoService
                .findWithoutSolrPaging(query, null, 1000, increment[0]);
        while (queryResult.getState() != null) {
            loopSave(queryResult.getData(), increment);
            queryResult = tbktDLieuMgrIoService
                    .findWithoutSolrPaging(query, queryResult.getState(), 1000, increment[0]);
            log.info("Complete migrate data with state: {}", queryResult.getState());
            log.info("Current increment: {}", increment[0]);
        }
        // Last page
        loopSave(queryResult.getData(), increment);
        log.info("Complete migrate data with state: {}", queryResult.getState());
        log.info("Current increment: {}", increment[0]);
    }

    private void loopSave(List<TbktdLieuMgr> queryResult, int[] increment) {
        queryResult.forEach(mgr -> {
            TbktdLieuNew toInsert = builder(mgr, increment[0] % NANOS_WITHIN_A_DAY, ChronoUnit.MILLIS);
            tbktDLieuNewIoService.saveAsync(toInsert);
            increment[0]++;
        });
    }

    @SuppressWarnings("SameParameterValue")
    private TbktdLieuNew builder(TbktdLieuMgr sourceData, long incrementValue, ChronoUnit unitType) {
        TbktdLieuNew result = mapper.map(sourceData);
        result.setNtao(sourceData.getNtao().plus(incrementValue, unitType));
        return result;
    }

    private Cache<Object, Object> getCache(String cacheName) {
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(cacheName);
        assert caffeineCache != null;
        return caffeineCache.getNativeCache();
    }

    @Deprecated
    @SuppressWarnings("unused")
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
}
