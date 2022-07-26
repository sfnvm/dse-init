package edu.sfnvm.dseinit.repository.inventory;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import edu.sfnvm.dseinit.repository.mapper.DummyKaiRepository;

public interface DummyInventoryMapper {
    @DaoFactory
    DummyKaiRepository dummyKaiRepository();
}
