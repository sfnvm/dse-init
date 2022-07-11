package edu.sfnvm.dseinit.service.io;

import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.repository.mapper.InventoryMapper;
import edu.sfnvm.dseinit.repository.mapper.TbktdLieuMgrRepository;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TbktdLieuMgrIoService {
    private final TbktdLieuMgrRepository tbktDLieuMgrRepository;

    @Autowired
    public TbktdLieuMgrIoService(InventoryMapper inventoryMapper) {
        this.tbktDLieuMgrRepository = inventoryMapper.tbktDLieuMgrRepository();
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
}
