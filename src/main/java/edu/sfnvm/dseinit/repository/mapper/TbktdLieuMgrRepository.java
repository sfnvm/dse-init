package edu.sfnvm.dseinit.repository.mapper;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;

import java.time.Instant;
import java.util.concurrent.CompletionStage;

@Dao
public interface TbktdLieuMgrRepository {
    @Select(customWhereClause = "mst = :mst AND ntao = :ntao")
    PagingIterable<TbktdLieuMgr> findByPartitionKeys(String mst, Instant ntao);

    @Insert
    CompletionStage<Void> saveAsync(TbktdLieuMgr tbktDLieuMgr);
}
