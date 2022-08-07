package edu.sfnvm.dseinit.repository.mapper;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.QueryProvider;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.repository.provider.CassandraBatchOpsProvider;
import edu.sfnvm.dseinit.repository.provider.SliceProvider;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Dao
public interface TbktdLieuMgrRepository {
  @Select(customWhereClause = "mst = :mst AND ntao = :ntao")
  PagingIterable<TbktdLieuMgr> findByPartitionKeys(String mst, Instant ntao);

  @Select
  Optional<TbktdLieuMgr> findByPartitionKeys(String mst, Instant ntao, UUID id);

  @Insert
  CompletionStage<Void> saveAsync(TbktdLieuMgr tbktDLieuMgr);

  @Insert
  BoundStatement boundStatementSave(TbktdLieuMgr dsTvanKdtKquaCtiet);

  @QueryProvider(providerClass = CassandraBatchOpsProvider.class)
  void executeBatch(List<BatchableStatement<?>> boundStatements, BatchType type, int size);

  @QueryProvider(providerClass = SliceProvider.class, entityHelpers = TbktdLieuMgr.class)
  PagingData<TbktdLieuMgr> findWithoutSolrPaging(String queryStr, String pagingState, int size);
}
