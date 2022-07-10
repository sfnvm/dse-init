package edu.sfnvm.dseinit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagingData<T> {
    private List<T> data;
    private String state;
    private int size;
    private Long total;
}
