package edu.sfnvm.dseinit.cache;

import edu.sfnvm.dseinit.model.TbktdLieuNew;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MgrTimeoutCache {
    @Cacheable(value = CacheConstants.RETRY, sync = true)
    public TbktdLieuNew cache(TbktdLieuNew entity) {
        log.info("Cache miss {} for value: {}", CacheConstants.RETRY, entity);
        return entity;
    }

    @CacheEvict(value = CacheConstants.RETRY, allEntries = true)
    public void clearCache() {
        log.info("Clear all cache: {}", CacheConstants.RETRY);
    }
}
