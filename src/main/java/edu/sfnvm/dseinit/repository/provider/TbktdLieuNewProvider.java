package edu.sfnvm.dseinit.repository.provider;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.mapper.MapperContext;
import com.datastax.oss.driver.api.mapper.entity.EntityHelper;
import com.datastax.oss.driver.api.mapper.entity.saving.NullSavingStrategy;
import edu.sfnvm.dseinit.model.TbktdLieuNew;

import java.util.List;

public class TbktdLieuNewProvider {
    private final CqlSession session;
    private final EntityHelper<TbktdLieuNew> entityHelper;

    public TbktdLieuNewProvider(MapperContext context, EntityHelper<TbktdLieuNew> entityHelper) {
        this.session = context.getSession();
        this.entityHelper = entityHelper;
    }

    public void saveList(List<TbktdLieuNew> items) throws Exception {
        PreparedStatement saveStatement = session.prepare(entityHelper.insert().build());
        for (TbktdLieuNew item : items) {
            BoundStatementBuilder boundStatementBuilder = saveStatement.boundStatementBuilder();
            entityHelper.set(item, boundStatementBuilder, NullSavingStrategy.DO_NOT_SET, false);
            session.execute(boundStatementBuilder.build());
        }
    }
}
