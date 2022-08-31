package edu.sfnvm.dseinit.service;

import edu.sfnvm.dseinit.cache.SaveTimeoutCache;
import edu.sfnvm.dseinit.cache.StateTimeoutCache;
import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.dto.StateTimeoutDto;
import edu.sfnvm.dseinit.dto.enums.SaveType;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.service.io.CacheIoService;
import edu.sfnvm.dseinit.service.io.TbktdLieuMgrIoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
public class RetryService {
    private final CacheIoService cacheIoService;
    private final TbktdLieuMgrIoService tbktDLieuMgrIoService;
    private final SaveTimeoutCache saveTimeoutCache;
    private final StateTimeoutCache stateTimeoutCache;

    @Autowired
    public RetryService(
        CacheIoService cacheIoService,
        TbktdLieuMgrIoService tbktDLieuMgrIoService,
        SaveTimeoutCache saveTimeoutCache,
        StateTimeoutCache stateTimeoutCache) {
        this.cacheIoService = cacheIoService;
        this.tbktDLieuMgrIoService = tbktDLieuMgrIoService;
        this.saveTimeoutCache = saveTimeoutCache;
        this.stateTimeoutCache = stateTimeoutCache;
    }

    public void retryCached(SaveType saveType) {
        // Retry save failed
        if (CollectionUtils.isEmpty(cacheIoService.getMgrTimeoutCache())) {
            log.info("Mgr failure cache clean. Retry skipped");
        } else {
            retryMgr(saveType);
        }

        // Retry paging failed
        if (CollectionUtils.isEmpty(cacheIoService.getStateTimeoutCache())) {
            log.info("State cache clean. Retry skipped");
        } else {
            retryState(saveType);
        }
    }

    private void retryMgr(SaveType saveType) {
        List<TbktdLieuMgr> toRetry = cacheIoService.getMgrTimeoutCache();
        saveTimeoutCache.clearCache();

        switch (saveType) {
            case SIMPLE: {
                for (TbktdLieuMgr tbktdLieuMgr : toRetry) {
                    tbktDLieuMgrIoService.save(tbktdLieuMgr);
                }
                break;
            }
            case ASYNC: {
                for (TbktdLieuMgr tbktdLieuMgr : toRetry) {
                    tbktDLieuMgrIoService.saveAsync(tbktdLieuMgr);
                }
                break;
            }
            case PREPARED: {
                tbktDLieuMgrIoService.saveList(toRetry);
                break;
            }
            default:
                log.error("Type not supported");
        }
    }

    private void retryState(SaveType saveType) {
        List<StateTimeoutDto> stateToRetry = cacheIoService.getStateTimeoutCache();
        stateTimeoutCache.clearCache();

        for (StateTimeoutDto dto : stateToRetry) {
            PagingData<TbktdLieuMgr> queryResult = tbktDLieuMgrIoService
                .findWithoutSolrPaging(
                    dto.getQuery(),
                    dto.getState(),
                    dto.getQuerySize(),
                    dto.getIncrement()
                );

            tbktDLieuMgrIoService.loopSave(
                queryResult.getData(),
                new int[]{dto.getIncrement()},
                saveType
            );
        }
    }
}
