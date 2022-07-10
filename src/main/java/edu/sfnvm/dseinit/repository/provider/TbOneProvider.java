package edu.sfnvm.dseinit.repository.provider;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.mapper.MapperContext;
import com.datastax.oss.driver.api.mapper.entity.EntityHelper;
import com.datastax.oss.protocol.internal.util.Bytes;
import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.model.TbOne;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TbOneProvider {
    private final CqlSession session;
    private final EntityHelper<TbOne> entityHelper;
    private final Map<String, PreparedStatement> psMap;

    public TbOneProvider(MapperContext context, EntityHelper<TbOne> entityHelper) {
        this.session = context.getSession();
        this.entityHelper = entityHelper;
        this.psMap = new HashMap<>();
    }

    public PagingData<TbOne> searchByQuery(String queryStr, String state, int limit) {
        if (!psMap.containsKey(queryStr)) {
            log.info("Init prepared statement");
            psMap.put(queryStr, session.prepare(queryStr));
        }

        PreparedStatement preparedStatement = psMap.get(queryStr);
        BoundStatementBuilder boundStatementBuilder = preparedStatement.boundStatementBuilder().setPageSize(limit);
        if (state != null) {
            boundStatementBuilder.setPagingState(Bytes.fromHexString(state));
        }
        ResultSet resultSet = session.execute(boundStatementBuilder.build());
        String nextPage = Bytes.toHexString(resultSet.getExecutionInfo().getPagingState());
        if (nextPage == null) {
            psMap.remove(queryStr);
        }

        int remaining = resultSet.getAvailableWithoutFetching();
        List<TbOne> datas = new ArrayList<>(remaining);
        if (remaining > 0) {
            for (Row row : resultSet) {
                datas.add(entityHelper.get(row, false));
                if (--remaining == 0) {
                    break;
                }
            }
        }

        PagingData<TbOne> pagedData = new PagingData<>();
        pagedData.setData(datas);
        pagedData.setState(nextPage);
        return pagedData;
    }
}
