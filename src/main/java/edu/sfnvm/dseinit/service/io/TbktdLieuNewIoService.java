package edu.sfnvm.dseinit.service.io;

import edu.sfnvm.dseinit.cache.MgrTimeoutCache;
import edu.sfnvm.dseinit.model.TbktdLieuNew;
import edu.sfnvm.dseinit.repository.mapper.InventoryMapper;
import edu.sfnvm.dseinit.repository.mapper.TbktdLieuNewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TbktdLieuNewIoService {
    private final TbktdLieuNewRepository tbktDLieuNewRepository;
    private final MgrTimeoutCache mgrTimeoutCache;

    @Autowired
    public TbktdLieuNewIoService(
            InventoryMapper inventoryMapper,
            MgrTimeoutCache mgrTimeoutCache) {
        this.tbktDLieuNewRepository = inventoryMapper.tbktDLieuNewRepository();
        this.mgrTimeoutCache = mgrTimeoutCache;
    }

    @Retryable(
            maxAttempts = 10,
            backoff = @Backoff(delay = 10000, multiplier = 2),
            value = {Exception.class}
    )
    public void saveAsync(TbktdLieuNew entity) {
        tbktDLieuNewRepository.saveAsync(entity).whenComplete((unused, ex) -> {
            if (ex != null) {
                log.error("Connot save entity {}", entity, ex);
                mgrTimeoutCache.cache(entity);
            }
        });
    }

    public void saveList(List<TbktdLieuNew> toInsertList) throws Exception {
        tbktDLieuNewRepository.saveList(toInsertList);
    }
}
