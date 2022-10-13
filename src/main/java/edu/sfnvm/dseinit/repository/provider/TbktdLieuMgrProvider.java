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
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TbktdLieuMgrProvider {
    private final CqlSession session;
    private final EntityHelper<TbktdLieuMgr> entityHelper;

    public TbktdLieuMgrProvider(MapperContext context, EntityHelper<TbktdLieuMgr> entityHelper) {
        this.session = context.getSession();
        this.entityHelper = entityHelper;
    }

    @SuppressWarnings("Duplicates")
    public void saveList(List<TbktdLieuMgr> items) {
        PreparedStatement saveStatement = session.prepare(entityHelper.insert().build());

        for (TbktdLieuMgr item : items) {
            BoundStatementBuilder boundStatementBuilder = saveStatement.boundStatementBuilder();
            entityHelper.set(item, boundStatementBuilder, NullSavingStrategy.DO_NOT_SET);
            try {
                session.execute(boundStatementBuilder.build());
            } catch (AllNodesFailedException | QueryExecutionException | QueryValidationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("Duplicates")
    public List<TbktdLieuMgr> saveListReturnFailed(List<TbktdLieuMgr> items) {
        List<TbktdLieuMgr> failed = new ArrayList<>();

        for (TbktdLieuMgr item : items) {
            try {
                PreparedStatement saveStatement = session.prepare(entityHelper.insert().build());
                BoundStatementBuilder boundStatementBuilder = saveStatement.boundStatementBuilder();
                entityHelper.set(item, boundStatementBuilder, NullSavingStrategy.DO_NOT_SET);
                session.execute(boundStatementBuilder.build());
            } catch (
                AllNodesFailedException
                | QueryExecutionException
                | QueryValidationException e) {
                log.error("Connection error for insert entity {}", item, e);
                failed.add(item);
            } catch (Exception e) {
                log.error("Timeout or unhandled error for insert entity {}", item, e);
                failed.add(item);
            }
        }

        return failed;
    }

    /**
     * <h2>Partial update</h2>
     */

    @SuppressWarnings("Duplicates")
    public List<TbktdLieuMgr> saveListDiffKhmshdReturnFailed(List<TbktdLieuMgr> items) {
        List<TbktdLieuMgr> failed = new ArrayList<>();
        int count = 0;

        for (TbktdLieuMgr item : items) {
            if (CollectionUtils.isEmpty(item.getTtctiet())) {
                continue;
            }

            if (item.getTtctiet().stream().anyMatch(i -> i.getKhmshd() != null)) {
                try {
                    PreparedStatement saveStatement = session.prepare(entityHelper.insert().build());
                    BoundStatementBuilder boundStatementBuilder = saveStatement.boundStatementBuilder();
                    entityHelper.set(item, boundStatementBuilder, NullSavingStrategy.DO_NOT_SET);
                    session.execute(boundStatementBuilder.build());
                    count++;
                } catch (
                    AllNodesFailedException
                    | QueryExecutionException
                    | QueryValidationException e) {
                    log.error("Connection error for insert entity {}", item, e);
                    failed.add(item);
                } catch (Exception e) {
                    log.error("Timeout or unhandled error for insert entity {}", item, e);
                    failed.add(item);
                }
            }
        }

        log.info("Saved records count: {}", count);
        return failed;
    }

    @SuppressWarnings("Duplicates")
    public List<TbktdLieuMgr> saveListDiffUdtLoiGchuReturnFailed(List<TbktdLieuMgr> items) {
        List<TbktdLieuMgr> failed = new ArrayList<>();
        int count = 0;

        for (TbktdLieuMgr item : items) {
            boolean invalidDsLoi = !CollectionUtils.isEmpty(item.getDsloi())
                && item.getDsloi().stream().anyMatch(u -> !StringUtils.isEmpty(u.getGchu()));

            boolean invalidTtctiet = !CollectionUtils.isEmpty(item.getTtctiet())
                && item.getTtctiet().stream()
                .anyMatch(
                    u -> !CollectionUtils.isEmpty(u.getDsloi())
                        && u.getDsloi().stream().anyMatch(l -> !StringUtils.isEmpty(l.getGchu())));

            if (invalidDsLoi || invalidTtctiet) {
                try {
                    PreparedStatement saveStatement = session.prepare(entityHelper.insert().build());
                    BoundStatementBuilder boundStatementBuilder = saveStatement.boundStatementBuilder();
                    entityHelper.set(item, boundStatementBuilder, NullSavingStrategy.DO_NOT_SET);
                    session.execute(boundStatementBuilder.build());
                    count++;
                } catch (
                    AllNodesFailedException
                    | QueryExecutionException
                    | QueryValidationException e) {
                    log.error("Connection error for insert entity {}", item, e);
                    failed.add(item);
                } catch (Exception e) {
                    log.error("Timeout or unhandled error for insert entity {}", item, e);
                    failed.add(item);
                }
            }
        }

        log.info("Saved records count: {}", count);
        return failed;
    }

    @SuppressWarnings("Duplicates")
    public List<TbktdLieuMgr> saveListDiffEnumsReturnFailed(List<TbktdLieuMgr> items) {
        List<TbktdLieuMgr> failed = new ArrayList<>();
        int count = 0;

        for (TbktdLieuMgr item : items) {
            // boolean invalidThop = item.getThop() != null && THKTDLieu.fromValue(item.getThop()) == null;
            // boolean invalidPtgui = item.getPtgui() != null && PTHDon.fromValue(item.getPtgui()) == null;
            // boolean invalidTtxly = item.getTtxly() != null && TTXLKTDLieu.fromValue(item.getTtxly()) == null;
            // boolean invalidTttbao = item.getTttbao() != null && TTTBKTDLieu.fromValue(item.getTttbao()) == null;
            //
            // if (invalidThop) log.info("invalidThop {}", item.getThop());
            // if (invalidPtgui) log.info("invalidPtgui {}", item.getPtgui());
            // if (invalidTtxly) log.info("invalidTtxly {}", item.getTtxly());
            // if (invalidTttbao) log.info("invalidTttbao {}", item.getTttbao());
            //
            // if (invalidThop || invalidPtgui || invalidTtxly || invalidTttbao) {
            try {
                PreparedStatement saveStatement = session.prepare(entityHelper.insert().build());
                BoundStatementBuilder boundStatementBuilder = saveStatement.boundStatementBuilder();
                entityHelper.set(item, boundStatementBuilder, NullSavingStrategy.DO_NOT_SET);
                session.execute(boundStatementBuilder.build());
                count++;
            } catch (
                AllNodesFailedException
                | QueryExecutionException
                | QueryValidationException e) {
                log.error("Connection error for insert entity {}", item, e);
                failed.add(item);
            } catch (Exception e) {
                log.error("Timeout or unhandled error for insert entity {}", item, e);
                failed.add(item);
            }
            // }
        }

        log.info("Saved records count: {}", count);
        return failed;
    }
}
