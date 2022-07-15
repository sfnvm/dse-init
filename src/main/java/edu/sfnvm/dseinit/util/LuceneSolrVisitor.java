package edu.sfnvm.dseinit.util;

import cz.jirutka.rsql.parser.ast.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.BytesRef;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TODO: Tunning<br>
 * https://docs.datastax.com/en/dse/6.8/cql/cql/cql_using/search_index/srchBestPractices.html
 */
@Slf4j
public class LuceneSolrVisitor implements RSQLVisitor<Query, Void> {
    private final Class<?> clazz;

    public LuceneSolrVisitor(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Query visit(AndNode andNode, Void param) {
        return this.createQuery(andNode);
    }

    @Override
    public Query visit(OrNode orNode, Void param) {
        return this.createQuery(orNode);
    }

    @Override
    public Query visit(ComparisonNode node, Void param) {
        return this.createQuery(node);
    }

    private Query createQuery(Node node) {
        if (node instanceof LogicalNode) {
            return this.createQuery((LogicalNode) node);
        }
        if (node instanceof ComparisonNode) {
            return this.createQuery((ComparisonNode) node);
        }
        return null;
    }

    private Query createQuery(LogicalNode node) {
        List<Query> queryList = node.getChildren()
                .stream()
                .map(this::createQuery)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (queryList.isEmpty()) {
            return null;
        }

        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

        if (node.getOperator() == LogicalOperator.AND) {
            for (Query query : queryList) {
                booleanQueryBuilder.add(new BooleanClause(query, Occur.MUST));
            }
        } else if (node.getOperator() == LogicalOperator.OR) {
            for (Query query : queryList) {
                booleanQueryBuilder.add(new BooleanClause(query, Occur.SHOULD));
            }
        }
        return booleanQueryBuilder.build();
    }

    private Query createQuery(ComparisonNode comparisonNode) {
        RsqlSearchOperation operation = RsqlSearchOperation.getSimpleOperator(comparisonNode.getOperator());

        if (operation == null) {
            return null;
        }

        String selector = comparisonNode.getSelector();
        List<String> arguments = comparisonNode.getArguments()
                .stream().map(this::normalizeString)
                .collect(Collectors.toList());

        Query query = null;

        switch (operation) {
            case EQUAL:
                query = new TermQuery(new Term(selector, arguments.get(0)));
                break;
            case NOT_EQUAL:
                query = new BooleanQuery.Builder()
                        .add(new TermQuery(new Term("*", "*")), Occur.MUST)
                        .add(new TermQuery(new Term(selector, arguments.get(0))), Occur.MUST_NOT)
                        .build();
                break;
            case GREATER_THAN:
                query = new TermRangeQuery(
                        selector,
                        new BytesRef(arguments.get(0).getBytes()),
                        null,
                        false, false);
                break;
            case GREATER_THAN_OR_EQUAL:
                query = new TermRangeQuery(
                        selector,
                        new BytesRef(arguments.get(0).getBytes()),
                        null,
                        true, true);
                break;
            case LESS_THAN:
                query = new TermRangeQuery(
                        selector,
                        null,
                        new BytesRef(arguments.get(0).getBytes()),
                        false, false);
                break;
            case LESS_THAN_OR_EQUAL:
                query = new TermRangeQuery(
                        selector,
                        null,
                        new BytesRef(arguments.get(0).getBytes()),
                        true, true);
                break;
            case IN:
                BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
                for (String v : arguments) {
                    queryBuilder.add(new TermQuery(new Term(selector, v)), Occur.SHOULD);
                }
                query = queryBuilder.build();
                break;
            default:
        }

        return query;
    }

    private String normalizeString(String arg) {
        try {
            arg = URLDecoder.decode(arg, StandardCharsets.UTF_8.toString());

            /* Remove single/double quote */
            if (arg.length() > 1) {
                if (arg.startsWith("\"") && arg.endsWith("\"")) {
                    arg = arg.substring(1, arg.length() - 1);
                }
                if (arg.startsWith("'") && arg.endsWith("'")) {
                    arg = arg.substring(1, arg.length() - 1);
                }
            }

            /* Replace asterisk */
            // if (arg.contains("*")) {
            //   arg = arg.replace('*', '%');
            // }

            return arg;
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
            return arg;
        }
    }
}
