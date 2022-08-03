package edu.sfnvm.dseinit.service;

import com.google.common.io.Resources;
import edu.sfnvm.dseinit.cache.MgrTimeoutCache;
import edu.sfnvm.dseinit.cache.StateTimeoutCache;
import edu.sfnvm.dseinit.constant.TimeMarkConstant;
import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.dto.StateTimeoutDto;
import edu.sfnvm.dseinit.dto.enums.SaveType;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RunnerService implements ApplicationRunner {
    /**
     * <h2>IO Services</h2>
     */
    private final TbktdLieuMgrIoService tbktdLieuMgrIoService;
    private final TbktdLieuNewIoService tbktdLieuNewIoService;

    private final MgrTimeoutCache mgrTimeoutCache;
    private final StateTimeoutCache stateTimeoutCache;

    private static final String SELECT_TBKTDL_BY_PARTITION =
            // "SELECT * FROM ks_hoadon.hddt_tbktdl_mgr WHERE mst = '%s' AND ntao = '%s'";
            "SELECT * FROM ks_hoadon.tbktdl_mgr WHERE mst = '%s' AND ntao = '%s'";

    private final TbktdLieuNewMapper mapper = Mappers.getMapper(TbktdLieuNewMapper.class);

    private static final boolean RUNNER = false;
    private static final String CASE = "INSERT_TARGET";

    @Autowired
    public RunnerService(
            TbktdLieuMgrIoService tbktdLieuMgrIoService,
            TbktdLieuNewIoService tbktdLieuNewIoService,
            MgrTimeoutCache mgrTimeoutCache,
            StateTimeoutCache stateTimeoutCache) {
        this.tbktdLieuMgrIoService = tbktdLieuMgrIoService;
        this.tbktdLieuNewIoService = tbktdLieuNewIoService;
        this.mgrTimeoutCache = mgrTimeoutCache;
        this.stateTimeoutCache = stateTimeoutCache;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (RUNNER) {
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
                    migrate(query, SaveType.PREPARED);
                });
                log.info("Mannual audit data done at: {}", Duration.between(start, Instant.now()));
            } else {
                log.info("Condition list empty, skip migrate tbktdlieu");
            }
        }
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

    @SuppressWarnings("SameParameterValue")
    private void migrate(String query, SaveType saveType) {
        final int[] increment = {1};
        PagingData<TbktdLieuMgr> queryResult = tbktdLieuMgrIoService
                .findWithoutSolrPaging(query, null, 1000, increment[0]);
        while (queryResult.getState() != null) {
            loopSave(queryResult.getData(), increment, saveType);
            queryResult = tbktdLieuMgrIoService
                    .findWithoutSolrPaging(query, queryResult.getState(), 1000, increment[0]);
            log.info("Complete migrate data with state: {}", queryResult.getState());
            log.info("Current increment: {}", increment[0]);
        }
        // Last page
        loopSave(queryResult.getData(), increment, saveType);
        log.info("Complete migrate data with state: {}", queryResult.getState());
        log.info("Current increment: {}", increment[0]);
    }

    void loopSave(List<TbktdLieuMgr> queryResult, int[] increment, SaveType saveType) {
        List<TbktdLieuNew> migratedList = queryResult.stream().map(mgr -> {
            TbktdLieuNew tmp = builder(
                    mgr, increment[0] % TimeMarkConstant.NANOS_WITHIN_A_DAY, ChronoUnit.MILLIS
            );
            increment[0]++;
            return tmp;
        }).collect(Collectors.toList());

        switch (saveType) {
            case ASYNC: {
                migratedList.forEach(tbktdLieuNewIoService::saveAsync);
                break;
            }
            case PREPARED: {
                tbktdLieuNewIoService.saveList(migratedList);
                break;
            }
            default:
                log.error("Save strategy not supported");
        }
    }

    @SuppressWarnings("SameParameterValue")
    private TbktdLieuNew builder(
            TbktdLieuMgr sourceData,
            long incrementValue,
            ChronoUnit unitType) {
        TbktdLieuNew result = mapper.map(sourceData);
        result.setNtao(sourceData.getNtao().plus(incrementValue, unitType));
        return result;
    }
}
