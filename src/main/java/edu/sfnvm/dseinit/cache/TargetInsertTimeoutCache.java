package edu.sfnvm.dseinit.cache;

import edu.sfnvm.dseinit.model.TbktdLieuNew;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TargetInsertTimeoutCache {
    @Cacheable(value = CacheConstants.TARGET_INSERT, sync = true)
    public TbktdLieuNew cache(TbktdLieuNew entity) {
        log.info("Cache miss {} for value: {}", CacheConstants.TARGET_INSERT, entity);
        return entity;
    }

    @CacheEvict(value = CacheConstants.TARGET_INSERT, allEntries = true)
    public void clearCache() {
        log.info("Clear all cache: {}", CacheConstants.TARGET_INSERT);
    }
}
