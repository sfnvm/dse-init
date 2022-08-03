package edu.sfnvm.dseinit.repository.provider;

import com.datastax.oss.driver.api.core.AllNodesFailedException;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.servererrors.QueryExecutionException;
import com.datastax.oss.driver.api.core.servererrors.QueryValidationException;
import com.datastax.oss.driver.api.mapper.MapperContext;
import com.datastax.oss.driver.api.mapper.entity.EntityHelper;
import com.datastax.oss.driver.api.mapper.entity.saving.NullSavingStrategy;
import edu.sfnvm.dseinit.model.TbktdLieuNew;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TbktdLieuNewProvider {
    private final CqlSession session;
    private final EntityHelper<TbktdLieuNew> entityHelper;

    public TbktdLieuNewProvider(MapperContext context, EntityHelper<TbktdLieuNew> entityHelper) {
        this.session = context.getSession();
        this.entityHelper = entityHelper;
    }

    @SuppressWarnings("Duplicates")
    public void saveList(List<TbktdLieuNew> items) {
        PreparedStatement saveStatement = session.prepare(entityHelper.insert().build());

        for (TbktdLieuNew item : items) {
            BoundStatementBuilder boundStatementBuilder = saveStatement.boundStatementBuilder();
            entityHelper.set(item, boundStatementBuilder, NullSavingStrategy.DO_NOT_SET, false);
            try {
                session.execute(boundStatementBuilder.build());
            } catch (AllNodesFailedException | QueryExecutionException | QueryValidationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("Duplicates")
    public List<TbktdLieuNew> saveListReturnFailed(List<TbktdLieuNew> items) {
        List<TbktdLieuNew> failed = new ArrayList<>();

        for (TbktdLieuNew item : items) {
            try {
                PreparedStatement saveStatement = session.prepare(entityHelper.insert().build());
                BoundStatementBuilder boundStatementBuilder = saveStatement.boundStatementBuilder();
                entityHelper.set(item, boundStatementBuilder, NullSavingStrategy.DO_NOT_SET, false);
                session.execute(boundStatementBuilder.build());
            } catch (AllNodesFailedException | QueryExecutionException | QueryValidationException e) {
                log.error("Connection error for insert entity {}", item, e);
                failed.add(item);
            } catch (Exception e) {
                log.error("Timeout or unhandled error for insert entity {}", item, e);
                failed.add(item);
            }
        }

        return failed;
    }
}
