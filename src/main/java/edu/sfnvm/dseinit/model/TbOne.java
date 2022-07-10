package edu.sfnvm.dseinit.model;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(defaultKeyspace = "sfnvm")
@CqlName("t_1")
public class TbOne {
    @PartitionKey
    private UUID id;

    @CqlName("p_1")
    private String p1;

    @CqlName("p_2")
    private String p2;

    @CqlName("p_3")
    private String p3;

    @CqlName("p_4")
    private Integer p4;

    @CqlName("p_5")
    private Long p5;
}
