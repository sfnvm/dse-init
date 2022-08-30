package edu.sfnvm.dseinit.model;

import com.datastax.oss.driver.api.mapper.annotations.*;
import com.datastax.oss.driver.api.mapper.entity.naming.NamingConvention;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Represent how time-based uuid can work properly under high throughput condition.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(defaultKeyspace = "sfnvm")
@NamingStrategy(convention = {NamingConvention.SNAKE_CASE_INSENSITIVE})
@CqlName("dummy_kai")
@ToString
public class DummyKai {
    @PartitionKey
    private String someUniqueValue;

    @ClusteringColumn
    private UUID timeUuid;

    private Instant createdTimestamp;
}
