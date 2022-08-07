package edu.sfnvm.dseinit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Description: Solr Query model
 */
@Data
@Builder
public class SolrSearch {
  @JsonProperty("q")
  private String query;

  @JsonInclude(Include.NON_NULL)
  @JsonProperty("sort")
  private String sort;

  /**
   * <a href="https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/search_index/cursorsDeepPaging.html">
   * REF
   * </a>
   * <br>
   * <p>
   * To dynamically enable paging when cql_solr_query_paging is set to off in dse.yaml,
   * set the Solr paging parameter to driver ("paging":"driver")
   * </p>
   */
  @JsonProperty("paging")
  @Builder.Default
  private String paging = "driver";
}
