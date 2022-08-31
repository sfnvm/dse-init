package edu.sfnvm.dseinit.repository.inventory;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import edu.sfnvm.dseinit.repository.mapper.TbktdLieuMgrRepository;

@Mapper
public interface InventoryMapper {
    @DaoFactory
    TbktdLieuMgrRepository tbktDLieuMgrRepository();
}
