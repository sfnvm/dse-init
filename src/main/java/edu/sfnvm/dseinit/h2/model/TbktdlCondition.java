package edu.sfnvm.dseinit.h2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TbktdlCondition {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long rnid;

    @Column(nullable = false)
    private String mst;

    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private Instant ntao;

    private Long wsize;

    private Double psize;
}
