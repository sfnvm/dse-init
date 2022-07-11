package edu.sfnvm.dseinit.service.io;

import edu.sfnvm.dseinit.model.TbktdLieuNew;
import edu.sfnvm.dseinit.repository.mapper.InventoryMapper;
import edu.sfnvm.dseinit.repository.mapper.TbktdLieuNewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TbktdLieuNewIoService {
    private final TbktdLieuNewRepository tbktDLieuNewRepository;

    @Autowired
    public TbktdLieuNewIoService(InventoryMapper inventoryMapper) {
        this.tbktDLieuNewRepository = inventoryMapper.tbktDLieuNewRepository();
    }

    public void saveAsync(TbktdLieuNew entity) {
        tbktDLieuNewRepository.saveAsync(entity).whenComplete((unused, ex) -> {
            if (ex != null) {
                log.error("Connot save entity {}", entity, ex);
            }
        });
    }
}
