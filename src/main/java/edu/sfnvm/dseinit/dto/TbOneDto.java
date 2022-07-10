package edu.sfnvm.dseinit.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbOneDto {
    private UUID id;

    private String p1;

    private String p2;

    private String p3;

    private Integer p4;

    private Long p5;
}
