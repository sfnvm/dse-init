package edu.sfnvm.dseinit.service.io;

import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import edu.sfnvm.dseinit.cache.SaveTimeoutCache;
import edu.sfnvm.dseinit.cache.StateTimeoutCache;
import edu.sfnvm.dseinit.constant.TimeMarkConstant;
import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.dto.StateTimeoutDto;
import edu.sfnvm.dseinit.dto.enums.SaveType;
import edu.sfnvm.dseinit.exception.ResourceNotFoundException;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.repository.inventory.InventoryMapper;
import edu.sfnvm.dseinit.repository.mapper.TbktdLieuMgrRepository;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TbktdLieuMgrIoService {
    private final TbktdLieuMgrRepository tbktDLieuMgrRepository;
    private final StateTimeoutCache stateTimeoutCache;
    private final SaveTimeoutCache saveTimeoutCache;

    @Autowired
    public TbktdLieuMgrIoService(
        InventoryMapper inventoryMapper,
        StateTimeoutCache stateTimeoutCache,
        SaveTimeoutCache saveTimeoutCache) {
        this.tbktDLieuMgrRepository = inventoryMapper.tbktDLieuMgrRepository();
        this.stateTimeoutCache = stateTimeoutCache;
        this.saveTimeoutCache = saveTimeoutCache;
    }

    /**
     * Search
     */
    public PagingData<TbktdLieuMgr> findWithoutSolrPaging(
        String queryStr, String pagingState, int size, int increment) {
        try {
            return findWithoutSolrPaging(queryStr, pagingState, size);
        } catch (Exception e) {
            stateTimeoutCache.cache(StateTimeoutDto.builder()
                .query(queryStr)
                .state(pagingState)
                .increment(increment)
                .querySize(size)
                .build());
            throw e;
        }
    }

    public PagingData<TbktdLieuMgr> findWithoutSolrPaging(
        String queryStr, String pagingState, int size) {
        return tbktDLieuMgrRepository.findWithoutSolrPaging(queryStr, pagingState, size);
    }

    /**
     * Find by partition keys
     */
    public TbktdLieuMgr findByPartitionKeys(String mst, Instant ntao, UUID id)
        throws ResourceNotFoundException {
        return tbktDLieuMgrRepository
            .findByPartitionKeys(mst, ntao, id)
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: TbktdLieuMgr"));
    }

    public List<TbktdLieuMgr> findByPartitionKeys(String mst, Instant ntao) {
        return tbktDLieuMgrRepository.findByPartitionKeys(mst, ntao).all();
    }

    public List<TbktdLieuMgr> findByPartitionKeys(List<Pair<String, Instant>> conditions) {
        return conditions.parallelStream()
            .flatMap(p -> findByPartitionKeys(p.getValue0(), p.getValue1()).stream())
            .collect(Collectors.toList());
    }

    public Map<Pair<String, Instant>, List<TbktdLieuMgr>> findAndTransformByPartitionKeys(
        List<Pair<String, Instant>> conditions) {
        Map<Pair<String, Instant>, List<TbktdLieuMgr>> result = new HashMap<>();
        conditions.forEach(p -> {
            List<TbktdLieuMgr> tmpRs = findByPartitionKeys(p.getValue0(), p.getValue1());
            if (!CollectionUtils.isEmpty(tmpRs)) {
                result.putIfAbsent(p, tmpRs);
            }
        });
        return result;
    }

    /**
     * Save
     */
    public void saveAsync(TbktdLieuMgr entity) {
        tbktDLieuMgrRepository.saveAsync(entity).whenComplete((unused, ex) -> {
            if (ex != null) {
                log.error("Connot save entity {}", entity, ex);
                saveTimeoutCache.cache(entity);
            }
        });
    }

    public void save(TbktdLieuMgr entity) {
        tbktDLieuMgrRepository.save(entity);
    }

    public BoundStatement boundStatementSave(TbktdLieuMgr entity) {
        return tbktDLieuMgrRepository.boundStatementSave(entity);
    }

    /**
     * Util save
     */
    public void saveList(List<TbktdLieuMgr> entityList) {
        List<TbktdLieuMgr> failed = tbktDLieuMgrRepository.saveListReturnFailed(entityList);
        if (!CollectionUtils.isEmpty(failed)) {
            failed.forEach(saveTimeoutCache::cache);
        }
    }

    public void executeBatch(List<BatchableStatement<?>> batch, BatchType type, int size) {
        tbktDLieuMgrRepository.executeBatch(batch, type, size);
    }

    public void loopSave(List<TbktdLieuMgr> queryResult, int[] increment, SaveType saveType) {
        List<TbktdLieuMgr> migratedList = queryResult.stream().map(mgr -> {
            TbktdLieuMgr tmp = builder(
                mgr, increment[0] % TimeMarkConstant.NANOS_WITHIN_A_DAY, ChronoUnit.MILLIS
            );
            increment[0]++;
            return tmp;
        }).collect(Collectors.toList());

        switch (saveType) {
            case ASYNC: {
                migratedList.forEach(this::saveAsync);
                break;
            }
            case PREPARED: {
                saveList(migratedList);
                break;
            }
            default:
                log.error("Save strategy not supported");
        }
    }

    @SuppressWarnings("SameParameterValue")
    private TbktdLieuMgr builder(
        TbktdLieuMgr sourceData,
        long incrementValue,
        ChronoUnit unitType) {
        sourceData.setNtao(sourceData.getNtao().plus(incrementValue, unitType));
        return sourceData;
    }
}
