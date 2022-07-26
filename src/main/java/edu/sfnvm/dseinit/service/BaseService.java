package edu.sfnvm.dseinit.service;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.ColumnRelationBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.jirutka.rsql.parser.ParseException;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import edu.sfnvm.dseinit.config.MessageTemplate;
import edu.sfnvm.dseinit.dto.SolrSearch;
import edu.sfnvm.dseinit.exception.PatternValidationException;
import edu.sfnvm.dseinit.util.DseRSQLVisitor;
import edu.sfnvm.dseinit.util.LuceneSolrVisitor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BaseService {
    @Autowired
    private MessageTemplate messageTemplate;

    private final RSQLParser rsqlParser = new RSQLParser();

    protected String message(String key, String... value) {
        return messageTemplate.message(key, value);
    }

    protected String messageVi(String key, String... value) {
        return messageTemplate.messageVi(key, value);
    }

    protected Select selectBuilder(String search, List<String> sorts, Class<?> clazz)
    throws PatternValidationException {
        if (search == null || search.isEmpty()) {
            search = "1==1";
        }

        Node rootNode = rsqlParser.parse(search);

        try {
            return rootNode.accept(new DseRSQLVisitor(clazz, sorts));
        } catch (Exception e) {
            throw new PatternValidationException(String.format("Search with keywords {%s} is invalid", search));
        }
    }

    /**
     * <a href="https://docs.datastax.com/en/dse/6.8/cql/cql/cql_using/search_index/siQuerySyntax.html">[REF1]</a>
     * <p>siQuerySyntax</p>
     * <br>
     * <a href="https://solr.apache.org/guide/6_6/the-standard-query-parser.html">[REF2]</a>
     * <p>the-standard-query-parser</p>
     */
    protected <T> SolrSearch solrQueryBuilder(String search, List<String> sorts, Query additionalQuery, Class<T> clazz) {
        if (search == null || search.isEmpty()) {
            search = "*==*";
        }
        Node rootNode = rsqlParser.parse(search);

        String sortsStr = CollectionUtils.isEmpty(sorts)
                ? null
                : String.join(",", sorts)
                .toLowerCase(Locale.ROOT)
                .replace(":", " ");

        Query visitorQuery = rootNode.accept(new LuceneSolrVisitor(clazz));

        if (additionalQuery == null) {
            return SolrSearch.builder()
                    .query(visitorQuery.toString())
                    .sort(sortsStr)
                    .build();
        } else {
            Query finalQuery = new BooleanQuery.Builder()
                    .add(new BooleanClause(visitorQuery, Occur.MUST))
                    .add(new BooleanClause(additionalQuery, Occur.MUST)).build();
            return SolrSearch.builder()
                    .query(finalQuery.toString())
                    .sort(sortsStr)
                    .build();
        }
    }

    protected Select selectFromSolrQuery(SolrSearch searchDto, Class<?> clazz)
    throws ParseException {
        // Extract keyspace
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new ParseException("Cannot found @Entity annotation in model class " + clazz.getSimpleName());
        }

        String keyspace = clazz.getAnnotation(Entity.class).defaultKeyspace();
        String table;

        // Extract table name
        if (clazz.isAnnotationPresent(CqlName.class)) {
            table = clazz.getAnnotation(CqlName.class).value();
        } else {
            table = clazz.getSimpleName().toLowerCase();
        }

        ObjectMapper mapper = new ObjectMapper();
        String queryStr = "";

        try {
            queryStr = mapper.writeValueAsString(searchDto);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ColumnRelationBuilder<Relation> colRelationBuilder = Relation.column("solr_query");
        Relation relation = colRelationBuilder.isEqualTo(QueryBuilder.literal(queryStr));

        return QueryBuilder.selectFrom(keyspace, table).all().where(relation);
    }

    protected String createRsqlQuery(List<String> values, String field) {
        if (CollectionUtils.isEmpty(values)) return "";

        int maxClauseCount = 1024;
        int totalEl = values.size(); // size of list
        int shouldClauseNums = (int) Math.ceil(totalEl * 1.0 / maxClauseCount); // the number of should clause
        List<String> shouldClauseList = new ArrayList<>(shouldClauseNums);

        for (int i = 0; i < shouldClauseNums; i++) {
            int start = i * maxClauseCount;
            int end = (i == shouldClauseNums - 1) ? totalEl : (i + 1) * maxClauseCount;
            String result = values.subList(start, end)
                    .stream()
                    .collect(Collectors.joining(",", String.format("%s=in=(", field), ")"));
            shouldClauseList.add(result);
        }

        String newSearch = String.format("(%s)", String.join(",", shouldClauseList));
        log.info("createRsqlQuery.search = {}", newSearch);

        return newSearch;
    }

}
