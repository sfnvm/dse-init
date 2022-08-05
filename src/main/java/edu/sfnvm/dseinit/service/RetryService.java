package edu.sfnvm.dseinit.service;

import edu.sfnvm.dseinit.cache.SourceStateTimeoutCache;
import edu.sfnvm.dseinit.cache.TargetInsertTimeoutCache;
import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.dto.StateTimeoutDto;
import edu.sfnvm.dseinit.dto.enums.SaveType;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.model.TbktdLieuNew;
import edu.sfnvm.dseinit.service.io.CacheIoService;
import edu.sfnvm.dseinit.service.io.TbktdLieuMgrIoService;
import edu.sfnvm.dseinit.service.io.TbktdLieuNewIoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
public class RetryService {
    private final CacheIoService cacheIoService;
    private final TbktdLieuNewIoService tbktDLieuNewIoService;
    private final TbktdLieuMgrIoService tbktDLieuMgrIoService;
    private final TargetInsertTimeoutCache targetInsertTimeoutCache;
    private final SourceStateTimeoutCache sourceStateTimeoutCache;

    @Autowired
    public RetryService(
            CacheIoService cacheIoService,
            TbktdLieuNewIoService tbktDLieuNewIoService,
            TbktdLieuMgrIoService tbktDLieuMgrIoService,
            TargetInsertTimeoutCache targetInsertTimeoutCache,
            SourceStateTimeoutCache sourceStateTimeoutCache) {
        this.cacheIoService = cacheIoService;
        this.tbktDLieuNewIoService = tbktDLieuNewIoService;
        this.tbktDLieuMgrIoService = tbktDLieuMgrIoService;
        this.targetInsertTimeoutCache = targetInsertTimeoutCache;
        this.sourceStateTimeoutCache = sourceStateTimeoutCache;
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
        List<TbktdLieuNew> toRetry = cacheIoService.getMgrTimeoutCache();
        targetInsertTimeoutCache.clearCache();

        switch (saveType) {
            case SIMPLE: {
                for (TbktdLieuNew tbktdLieuNew : toRetry) {
                    tbktDLieuNewIoService.save(tbktdLieuNew);
                }
                break;
            }
            case ASYNC: {
                for (TbktdLieuNew tbktdLieuNew : toRetry) {
                    tbktDLieuNewIoService.saveAsync(tbktdLieuNew);
                }
                break;
            }
            case PREPARED: {
                tbktDLieuNewIoService.saveList(toRetry);
                break;
            }
            default:
                log.error("Type not supported");
        }
    }

    private void retryState(SaveType saveType) {
        List<StateTimeoutDto> stateToRetry = cacheIoService.getStateTimeoutCache();
        sourceStateTimeoutCache.clearCache();

        for (StateTimeoutDto dto : stateToRetry) {
            PagingData<TbktdLieuMgr> queryResult = tbktDLieuMgrIoService.findWithoutSolrPaging(
                    dto.getQuery(),
                    dto.getState(),
                    dto.getQuerySize(),
                    dto.getIncrement());
            tbktDLieuNewIoService.loopSave(queryResult.getData(), new int[]{dto.getIncrement()}, saveType);
        }
    }
}
