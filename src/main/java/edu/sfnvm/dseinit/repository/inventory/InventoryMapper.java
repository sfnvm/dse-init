package edu.sfnvm.dseinit.repository.inventory;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import edu.sfnvm.dseinit.repository.mapper.TbktdLieuMgrRepository;
import edu.sfnvm.dseinit.repository.mapper.TbktdLieuNewRepository;

@Mapper
public interface InventoryMapper {
    @DaoFactory
    TbktdLieuMgrRepository tbktDLieuMgrRepository();

    @DaoFactory
    TbktdLieuNewRepository tbktDLieuNewRepository();
}
