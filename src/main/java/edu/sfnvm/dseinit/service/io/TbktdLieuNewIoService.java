package edu.sfnvm.dseinit.service.io;

import edu.sfnvm.dseinit.cache.TargetInsertTimeoutCache;
import edu.sfnvm.dseinit.constant.TimeMarkConstant;
import edu.sfnvm.dseinit.dto.enums.SaveType;
import edu.sfnvm.dseinit.mapper.TbktdLieuNewMapper;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.model.TbktdLieuNew;
import edu.sfnvm.dseinit.repository.inventory.InventoryMapper;
import edu.sfnvm.dseinit.repository.mapper.TbktdLieuNewRepository;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TbktdLieuNewIoService {
  private final TbktdLieuNewRepository tbktDLieuNewRepository;
  private final TargetInsertTimeoutCache targetInsertTimeoutCache;

  private final TbktdLieuNewMapper mapper = Mappers.getMapper(TbktdLieuNewMapper.class);

  @Autowired
  public TbktdLieuNewIoService(
    InventoryMapper inventoryMapper,
    TargetInsertTimeoutCache targetInsertTimeoutCache) {
    this.tbktDLieuNewRepository = inventoryMapper.tbktDLieuNewRepository();
    this.targetInsertTimeoutCache = targetInsertTimeoutCache;
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
        targetInsertTimeoutCache.cache(entity);
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
      failed.forEach(targetInsertTimeoutCache::cache);
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

  public void loopSave(List<TbktdLieuMgr> queryResult, int[] increment, SaveType saveType) {
    List<TbktdLieuNew> migratedList = queryResult.stream().map(mgr -> {
      TbktdLieuNew tmp = builder(
        mgr, increment[0] % TimeMarkConstant.NANOS_WITHIN_A_DAY, ChronoUnit.MILLIS
      );
      increment[0]++;
      return tmp;
    }).collect(Collectors.toList());

    switch (saveType) {
      case ASYNC: {
        migratedList.forEach(this::saveAsync);
        break;
      }
      case PREPARED: {
        saveList(migratedList);
        break;
      }
      default:
        log.error("Save strategy not supported");
    }
  }

  @SuppressWarnings("SameParameterValue")
  private TbktdLieuNew builder(TbktdLieuMgr sourceData, long incrementValue, ChronoUnit unitType) {
    TbktdLieuNew result = mapper.map(sourceData);
    result.setNtao(sourceData.getNtao().plus(incrementValue, unitType));
    return result;
  }
}
