package edu.sfnvm.dseinit.service.io;

import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import edu.sfnvm.dseinit.cache.StateTimeoutCache;
import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.dto.StateTimeoutDto;
import edu.sfnvm.dseinit.exception.ResourceNotFoundException;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.repository.inventory.InventoryMapper;
import edu.sfnvm.dseinit.repository.mapper.TbktdLieuMgrRepository;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
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

    @Autowired
    public TbktdLieuMgrIoService(
            InventoryMapper inventoryMapper,
            StateTimeoutCache stateTimeoutCache) {
        this.tbktDLieuMgrRepository = inventoryMapper.tbktDLieuMgrRepository();
        this.stateTimeoutCache = stateTimeoutCache;
    }

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

    // @Retryable(
    // 		maxAttempts = 5,
    // 		backoff = @Backoff(delay = 10000, multiplier = 2),
    // 		value = {Exception.class}
    // )
    public PagingData<TbktdLieuMgr> findWithoutSolrPaging(
            String queryStr, String pagingState, int size) {
        return tbktDLieuMgrRepository.findWithoutSolrPaging(queryStr, pagingState, size);
    }

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

    public void saveAsync(TbktdLieuMgr entity) {
        tbktDLieuMgrRepository.saveAsync(entity).whenComplete((unused, ex) -> {
            if (ex != null) {
                log.error("Connot save entity {}", entity, ex);
            }
        });
    }

    public BoundStatement boundStatementSave(TbktdLieuMgr entity) {
        return tbktDLieuMgrRepository.boundStatementSave(entity);
    }

    public void executeBatch(List<BatchableStatement<?>> batch, BatchType type, int size) {
        tbktDLieuMgrRepository.executeBatch(batch, type, size);
    }
}
