package edu.sfnvm.dseinit.repository.mapper;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import edu.sfnvm.dseinit.model.TbktdLieuNew;

import java.util.concurrent.CompletionStage;

@Dao
public interface TbktdLieuNewRepository {
    @Insert
    CompletionStage<Void> saveAsync(TbktdLieuNew tbktDLieuNew);
}
