package edu.sfnvm.dseinit.repository.mapper;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface InventoryMapper {
    @DaoFactory
    TbOneRepository tOneRepository();
}
