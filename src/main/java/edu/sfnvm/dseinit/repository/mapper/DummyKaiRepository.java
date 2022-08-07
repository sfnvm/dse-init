package edu.sfnvm.dseinit.repository.mapper;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import edu.sfnvm.dseinit.model.DummyKai;

import java.util.concurrent.CompletionStage;

@Dao
public interface DummyKaiRepository {
  @Insert
  void saveSync(DummyKai entity);

  @Insert
  CompletionStage<Void> saveAsync(DummyKai entity);

  @Delete(entityClass = DummyKai.class)
  void deleteByPartition(String pk);

  @Select(customWhereClause = "some_unique_value = :pk", orderBy = {"time_uuid ASC"})
  PagingIterable<DummyKai> selectAscByTimeUuid(String pk);

  @Select(customWhereClause = "some_unique_value = :pk", orderBy = {"time_uuid DESC"})
  PagingIterable<DummyKai> selectDescByTimeUuid(String pk);
}
