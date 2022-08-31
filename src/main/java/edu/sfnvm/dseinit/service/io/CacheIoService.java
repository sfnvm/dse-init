package edu.sfnvm.dseinit.service.io;

import com.github.benmanes.caffeine.cache.Cache;
import edu.sfnvm.dseinit.cache.SaveTimeoutCache;
import edu.sfnvm.dseinit.cache.StateTimeoutCache;
import edu.sfnvm.dseinit.constant.CacheConstants;
import edu.sfnvm.dseinit.dto.StateTimeoutDto;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
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
    private final SaveTimeoutCache saveTimeoutCache;
    private final StateTimeoutCache stateTimeoutCache;

    @Autowired
    public CacheIoService(
        CacheManager cacheManager,
        SaveTimeoutCache saveTimeoutCache,
        StateTimeoutCache stateTimeoutCache) {
        this.cacheManager = cacheManager;
        this.saveTimeoutCache = saveTimeoutCache;
        this.stateTimeoutCache = stateTimeoutCache;
    }

    public List<TbktdLieuMgr> getMgrTimeoutCache() {
        Cache<Object, Object> nativeCache = getCache(CacheConstants.SAVE);
        if (nativeCache == null) {
            return new ArrayList<>();
        }
        List<TbktdLieuMgr> cachedList = nativeCache.asMap().values()
            .stream()
            .map(o -> (TbktdLieuMgr) o)
            .collect(Collectors.toList());
        log.debug("Cache current size {}", cachedList.size());
        return cachedList;
    }

    public List<StateTimeoutDto> getStateTimeoutCache() {
        Cache<Object, Object> nativeCache = getCache(CacheConstants.STATE);
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

    public TbktdLieuMgr putMgrTimeoutCache(TbktdLieuMgr tbktdLieuNew) {
        return saveTimeoutCache.cache(tbktdLieuNew);
    }

    public StateTimeoutDto putStateTimeoutCache(StateTimeoutDto stateTimeoutDto) {
        stateTimeoutCache.cache(stateTimeoutDto);
        return stateTimeoutCache.cache(stateTimeoutDto);
    }

    public void clearMgrTimeoutCache() {
        saveTimeoutCache.clearCache();
    }

    public void clearStateTimeoutCache() {
        stateTimeoutCache.clearCache();
    }
}
