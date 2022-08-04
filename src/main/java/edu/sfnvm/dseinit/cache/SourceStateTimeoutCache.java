package edu.sfnvm.dseinit.cache;

import edu.sfnvm.dseinit.constant.CacheConstants;
import edu.sfnvm.dseinit.dto.StateTimeoutDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SourceStateTimeoutCache {
    @Cacheable(value = CacheConstants.SOURCE_STATE, sync = true)
    public StateTimeoutDto cache(StateTimeoutDto stateTimeoutDto) {
        log.info("Cache miss {} for value: {}", CacheConstants.SOURCE_STATE, stateTimeoutDto);
        return stateTimeoutDto;
    }

    @CacheEvict(value = CacheConstants.SOURCE_STATE, allEntries = true)
    public void clearCache() {
        log.info("Clear all cache: {}", CacheConstants.SOURCE_STATE);
    }
}
