package edu.sfnvm.dseinit.service.io;

import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.model.TbOne;
import edu.sfnvm.dseinit.repository.mapper.InventoryMapper;
import edu.sfnvm.dseinit.repository.mapper.TbOneRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class TbOneIoService {
    private final TbOneRepository tbOneRepository;

    @Autowired
    public TbOneIoService(InventoryMapper inventoryMapper) {
        tbOneRepository = inventoryMapper.tOneRepository();
    }

    public void save(TbOne entity) {
        tbOneRepository.save(entity);
    }

    public Optional<TbOne> findByPartition(UUID id) {
        return tbOneRepository.findByPartition(id);
    }

    public PagingData<TbOne> findSlicePaging(String queryStr, String pagingState, int size) {
        return tbOneRepository.findSlicePaging(queryStr, pagingState, size);
    }

    public PagingData<TbOne> findByRawQuery(String rawQuery, String pagingState, int limit) {
        return tbOneRepository.searchByQuery(rawQuery, pagingState, limit);
    }
}
