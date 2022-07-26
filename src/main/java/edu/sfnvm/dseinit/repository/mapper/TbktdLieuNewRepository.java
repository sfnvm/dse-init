package edu.sfnvm.dseinit.repository.mapper;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.QueryProvider;
import edu.sfnvm.dseinit.model.TbktdLieuNew;
import edu.sfnvm.dseinit.repository.provider.TbktdLieuNewProvider;

import java.util.List;
import java.util.concurrent.CompletionStage;

@Dao
public interface TbktdLieuNewRepository {
    @Insert
    CompletionStage<Void> saveAsync(TbktdLieuNew entity);

    @Insert
    void save(TbktdLieuNew entity);

    @QueryProvider(providerClass = TbktdLieuNewProvider.class, entityHelpers = TbktdLieuNew.class)
    void saveList(List<TbktdLieuNew> items);

    @QueryProvider(providerClass = TbktdLieuNewProvider.class, entityHelpers = TbktdLieuNew.class)
    List<TbktdLieuNew> saveListReturnFailed(List<TbktdLieuNew> items);
}
