package edu.sfnvm.dseinit.repository.provider;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.mapper.MapperContext;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.entity.EntityHelper;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import edu.sfnvm.dseinit.dto.PagingData;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class SliceProvider<T> {
    private final CqlSession session;
    private final EntityHelper<T> entityHelper;
    private final Select selectStart;
    private final List<String> partitionKeys;

    public SliceProvider(MapperContext context, EntityHelper<T> entityHelper) {
        this.session = context.getSession();
        this.entityHelper = entityHelper;
        this.selectStart = entityHelper.selectStart();

        this.partitionKeys = new ArrayList<>();
        Field[] fields = entityHelper.getEntityClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(PartitionKey.class)) {
                partitionKeys.add(field.getName());
            }
        }
    }

    public PagingData<T> findSlicePaging(String queryStr, String pagingState, int size) {
        SimpleStatementBuilder statementBuilder = SimpleStatement.builder(queryStr).setPageSize(size);

        if (pagingState != null) {
            statementBuilder.setPagingState(PagingState.fromString(pagingState).getRawPagingState());
        }

        ResultSet rs = session.execute(statementBuilder.build());

        Long total = null;
        try {
            total = rs.getExecutionInfo().getIncomingPayload().get("DSESearch.numFound").getLong();
        } catch (Exception ex) {
            log.info("Trying to count records");
            try {
                queryStr = queryStr.toLowerCase();
                String rawQuery = queryStr.replace("*", "count(*)");

                String[] queryParts = rawQuery.split("order by");
                String finalQuery = queryParts[0];
                if (rawQuery.contains("where")) {
                    finalQuery += " and solr_query='*:*'";
                } else {
                    finalQuery += " where solr_query='*:*'";
                }
                ResultSet totalRs = session.execute(finalQuery);
                total = Objects.requireNonNull(totalRs.one()).getLong(0);
            } catch (Exception e) {
                log.error("Cannot execute query", e);
            }
            log.error("Cannot get total size from Resulset with query: " + queryStr);
        }

        PagingState newPagingState = rs.getExecutionInfo().getSafePagingState();
        List<T> data = new ArrayList<>();
        while (rs.getAvailableWithoutFetching() > 0) {
            Row row = rs.one();
            if (row != null) {
                data.add(entityHelper.get(row, false));
            }
        }

        PagingData<T> pagingData = new PagingData<>();
        pagingData.setData(data);
        pagingData.setState(newPagingState == null ? null : newPagingState.toString());
        pagingData.setSize(data.size());
        pagingData.setTotal(total);

        return pagingData;
    }
}
