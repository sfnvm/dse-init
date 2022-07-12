package edu.sfnvm.dseinit.repository.provider;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.datastax.oss.driver.api.mapper.MapperContext;
import com.google.common.collect.Lists;

import java.util.List;

public class CassandraBatchOpsProvider {
    private final CqlSession session;

    public CassandraBatchOpsProvider(MapperContext context) {
        this.session = context.getSession();
    }

    public void executeBatch(List<BatchableStatement<?>> orgBatch, BatchType type, int size) {
        Lists.partition(orgBatch, size).forEach(batchableStatements -> {
            BatchStatement tmpBatch = BatchStatement
                    .builder(type == null ? BatchType.LOGGED : type)
                    .addStatements(batchableStatements)
                    .build();
            session.execute(tmpBatch);
        });
    }
}
