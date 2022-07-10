package edu.sfnvm.dseinit.repository.mapper;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.QueryProvider;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.model.TbOne;
import edu.sfnvm.dseinit.repository.provider.SliceProvider;
import edu.sfnvm.dseinit.repository.provider.TbOneProvider;

import java.util.Optional;
import java.util.UUID;

@Dao
public interface TbOneRepository {
    @Insert
    void save(TbOne tbOne);

    @Select
    Optional<TbOne> findByPartition(UUID id);

    @QueryProvider(providerClass = SliceProvider.class, entityHelpers = TbOne.class)
    PagingData<TbOne> findSlicePaging(String queryStr, String pagingState, int size);

    @QueryProvider(providerClass = TbOneProvider.class, entityHelpers = TbOne.class)
    PagingData<TbOne> searchByQuery(String queryStr, String state, int limit);
}
