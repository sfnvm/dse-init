package edu.sfnvm.dseinit.cache;

import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StateTimeoutCache {
    @Cacheable(value = CacheConstants.STATE, sync = true)
    public Pair<String, Integer> cache(Pair<String, Integer> value) {
        log.info("Cache miss {} for value: {}", CacheConstants.STATE, value);
        return value;
    }

    @CacheEvict(value = CacheConstants.STATE, allEntries = true)
    public void clearCache() {
        log.info("Clear all cache: {}", CacheConstants.STATE);
    }
}
