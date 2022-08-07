package edu.sfnvm.dseinit.service.io;

import com.github.benmanes.caffeine.cache.Cache;
import edu.sfnvm.dseinit.cache.SourceStateTimeoutCache;
import edu.sfnvm.dseinit.cache.TargetInsertTimeoutCache;
import edu.sfnvm.dseinit.constant.CacheConstants;
import edu.sfnvm.dseinit.dto.StateTimeoutDto;
import edu.sfnvm.dseinit.model.TbktdLieuNew;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CacheIoService {
  private final CacheManager cacheManager;
  private final TargetInsertTimeoutCache targetInsertTimeoutCache;
  private final SourceStateTimeoutCache sourceStateTimeoutCache;

  @Autowired
  public CacheIoService(
    CacheManager cacheManager,
    TargetInsertTimeoutCache targetInsertTimeoutCache,
    SourceStateTimeoutCache sourceStateTimeoutCache) {
    this.cacheManager = cacheManager;
    this.targetInsertTimeoutCache = targetInsertTimeoutCache;
    this.sourceStateTimeoutCache = sourceStateTimeoutCache;
  }

  public List<TbktdLieuNew> getMgrTimeoutCache() {
    Cache<Object, Object> nativeCache = getCache(CacheConstants.TARGET_SAVE);
    if (nativeCache == null) {
      return new ArrayList<>();
    }
    List<TbktdLieuNew> cachedList = nativeCache.asMap().values()
      .stream()
      .map(o -> (TbktdLieuNew) o)
      .collect(Collectors.toList());
    log.debug("Cache current size {}", cachedList.size());
    return cachedList;
  }

  public List<StateTimeoutDto> getStateTimeoutCache() {
    Cache<Object, Object> nativeCache = getCache(CacheConstants.SOURCE_STATE);
    if (nativeCache == null) {
      return new ArrayList<>();
    }
    List<StateTimeoutDto> cachedList = nativeCache.asMap().values()
      .stream()
      .map(o -> (StateTimeoutDto) o)
      .collect(Collectors.toList());
    log.debug("Cache current size {}", cachedList.size());
    return cachedList;
  }

  private Cache<Object, Object> getCache(String cacheName) {
    CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(cacheName);
    if (caffeineCache == null) {
      return null;
    } else {
      return caffeineCache.getNativeCache();
    }
  }

  public TbktdLieuNew putMgrTimeoutCache(TbktdLieuNew tbktdLieuNew) {
    return targetInsertTimeoutCache.cache(tbktdLieuNew);
  }

  public StateTimeoutDto putStateTimeoutCache(StateTimeoutDto stateTimeoutDto) {
    sourceStateTimeoutCache.cache(stateTimeoutDto);
    return sourceStateTimeoutCache.cache(stateTimeoutDto);
  }

  public void clearMgrTimeoutCache() {
    targetInsertTimeoutCache.clearCache();
  }

  public void clearStateTimeoutCache() {
    sourceStateTimeoutCache.clearCache();
  }
}
