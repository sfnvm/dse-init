package edu.sfnvm.dseinit.service.io;

import edu.sfnvm.dseinit.cache.MgrTimeoutCache;
import edu.sfnvm.dseinit.dto.enums.SaveType;
import edu.sfnvm.dseinit.model.TbktdLieuNew;
import edu.sfnvm.dseinit.repository.inventory.InventoryMapper;
import edu.sfnvm.dseinit.repository.mapper.TbktdLieuNewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

    /**
     * <b>Tested throughput: 10K easyrandom records</b>
     * <ul>
     *   <li>Round 01: No data</li>
     *   <li>Round 02: No data</li>
     *   <li>Round 03: No data</li>
     *   <li>Round 04: No data</li>
     *   <li>Round 05: No data</li>
     * </ul>
     */
    public void saveAsync(TbktdLieuNew entity) {
        tbktDLieuNewRepository.saveAsync(entity).whenComplete((unused, ex) -> {
            if (ex != null) {
                log.error("Connot save entity {}", entity, ex);
                mgrTimeoutCache.cache(entity);
            }
        });
    }

    /**
     * <b>Tested throughput: 10K easyrandom records</b>
     * <ul>
     *   <li>Round 01: No data</li>
     *   <li>Round 02: No data</li>
     *   <li>Round 03: No data</li>
     *   <li>Round 04: No data</li>
     *   <li>Round 05: No data</li>
     * </ul>
     */
    public void save(TbktdLieuNew entity) {
        tbktDLieuNewRepository.save(entity);
    }

    /**
     * <b>Test through put: 10K easyrandom records</b>
     * <ul>
     *   <li>Round 01: PT3.106S</li>
     *   <li>Round 02: PT3.092S</li>
     *   <li>Round 03: No data</li>
     *   <li>Round 04: No data</li>
     *   <li>Round 05: No data</li>
     * </ul>
     */
    public void saveList(List<TbktdLieuNew> entityList) {
        List<TbktdLieuNew> failed = tbktDLieuNewRepository.saveListReturnFailed(entityList);
        if (!CollectionUtils.isEmpty(failed)) {
            failed.forEach(mgrTimeoutCache::cache);
        }
    }

    public void benchmarkSave(List<TbktdLieuNew> entityList, SaveType type) {
        switch (type) {
            case SIMPLE:
                for (TbktdLieuNew e : entityList) {
                    save(e);
                }
                break;
            case ASYNC:
                for (TbktdLieuNew e : entityList) {
                    saveAsync(e);
                }
                break;
            case PREPARED:
                saveList(entityList);
                break;
            default:
                log.error("Type not supported");
        }
    }
}
