package edu.sfnvm.dseinit.cache;

import edu.sfnvm.dseinit.constant.CacheConstants;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SaveTimeoutCache {
  @Cacheable(value = CacheConstants.SAVE, sync = true)
  public TbktdLieuMgr cache(TbktdLieuMgr entity) {
    log.info("Cache miss {} for value: {}", CacheConstants.SAVE, entity);
    return entity;
  }

  @CacheEvict(value = CacheConstants.SAVE, allEntries = true)
  public void clearCache() {
    log.info("Clear all cache: {}", CacheConstants.SAVE);
  }
}
