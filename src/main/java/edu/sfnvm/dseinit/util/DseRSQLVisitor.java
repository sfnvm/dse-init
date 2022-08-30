package edu.sfnvm.dseinit.util;

import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.ColumnRelationBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import cz.jirutka.rsql.parser.ast.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;

@Slf4j
public class DseRSQLVisitor implements RSQLVisitor<Select, Void> {
    private final Class<?> clazz;
    private final String keyspace;
    private final String table;
    private final List<String> sorts;

    public DseRSQLVisitor(Class<?> clazz, List<String> sorts) throws Exception {
        this.clazz = clazz;
        this.sorts = !CollectionUtils.isEmpty(sorts) ? sorts : new ArrayList<>();

        // Extract keyspace
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new Exception("Cannot found @Entity annotation in model class " + clazz.getSimpleName());
        }
        this.keyspace = clazz.getAnnotation(Entity.class).defaultKeyspace();

        // Extract table name
        if (clazz.isAnnotationPresent(CqlName.class)) {
            this.table = clazz.getAnnotation(CqlName.class).value();
        } else {
            this.table = clazz.getSimpleName().toLowerCase();
        }
    }

    @Override
    public Select visit(AndNode andNode, Void unused) {
        return visit(createSelect(andNode));
    }

    @Override
    public Select visit(OrNode orNode, Void unused) {
        return null;
    }

    @Override
    public Select visit(ComparisonNode comparisonNode, Void unused) {
        return visit(createSelect(comparisonNode));
    }

    private Select visit(List<Relation> relations) {
        Select select;
        if (relations != null && !relations.isEmpty()) {
            select = QueryBuilder.selectFrom(keyspace, table).all().where(relations);
        } else {
            select = QueryBuilder.selectFrom(keyspace, table).all();
        }

        if (!CollectionUtils.isEmpty(sorts)) {
            for (String str : sorts) {
                String[] array = str.trim().split("\\s*:\\s*");
                select = select.orderBy(array[0], ClusteringOrder.valueOf(array[1].toUpperCase()));
            }
        }
        return select;
    }

    private List<Relation> createSelect(Node node) {
        if (node instanceof LogicalNode) {
            return createSelect((LogicalNode) node);
        }
        if (node instanceof ComparisonNode) {
            return createSelect((ComparisonNode) node);
        }
        return null;
    }

    private List<Relation> createSelect(LogicalNode logicalNode) {
        return logicalNode.getChildren()
            .parallelStream()
            .map(this::createSelect)
            .flatMap(Collection::parallelStream)
            .collect(Collectors.toList());
    }

    private Function<String, Object> convertUUID() {
        return input -> {
            try {
                return UUID.fromString(input);
            } catch (Exception ex) {
                ex.printStackTrace();
                return input;
            }
        };
    }

    private Function<String, Object> convertDouble() {
        return input -> {
            try {
                return NumberUtils.createDouble(input);
            } catch (Exception ex) {
                ex.printStackTrace();
                return input;
            }
        };
    }

    private Function<String, Object> convertDateTime() {
        return input -> {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm:ss", Locale.US);
                return LocalDateTime.parse(input, formatter).atZone(ZoneId.systemDefault()).toInstant();
            } catch (Exception ex) {
                ex.printStackTrace();
                return input;
            }
        };

    }

    private Function<String, Object> convertNumber() {
        return input -> {
            try {
                return NumberUtils.createNumber(input);
            } catch (Exception ex) {
                ex.printStackTrace();
                return input;
            }
        };
    }

    private Function<String, Object> convertBigDecimal() {
        return input -> {
            try {
                return NumberUtils.createBigDecimal(input);
            } catch (Exception ex) {
                ex.printStackTrace();
                return input;
            }
        };
    }

    private List<Object> convertValue(List<String> inputs, Class<?> clazz) {
        Function<String, Object> converter;
        if (clazz.equals(Instant.class)) {
            converter = convertDateTime();
        } else if (clazz.equals(BigDecimal.class)) {
            converter = convertBigDecimal();
        } else if (clazz.equals(Double.class)) {
            converter = convertDouble();
        } else if (clazz.equals(UUID.class)) {
            converter = convertUUID();
        } else if (!clazz.equals(String.class)) {
            converter = convertNumber();
        } else {
            converter = s -> s;
        }
        return inputs.parallelStream()
            .map(converter)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<Relation> createSelect(ComparisonNode comparisonNode) {
        RsqlSearchOperation operation = RsqlSearchOperation.getSimpleOperator(comparisonNode.getOperator());
        if (operation == null) {
            return null;
        }

        String selector = comparisonNode.getSelector();
        List<String> arguments = comparisonNode.getArguments();

        // convert values
        List<Object> values;
        try {
            Field field = clazz.getDeclaredField(selector.toLowerCase());
            values = convertValue(arguments, field.getType());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

        Object argument = values.size() == 1 ? values.get(0) : values;

        List<Relation> relations = new ArrayList<>();
        ColumnRelationBuilder<Relation> relation = Relation.column(selector);
        switch (operation) {
            case EQUAL:
                if (argument instanceof String && argument.toString().contains("*")) {
                    argument = argument.toString().replace('*', '%');
                    relations.add(relation.like(literal(argument)));
                } else {
                    relations.add(relation.isEqualTo(literal(argument)));
                }
                break;
            case NOT_EQUAL:
                if (argument instanceof String && argument.toString().equals("null")) {
                    relations.add(relation.isNotNull());
                } else {
                    relations.add(relation.isNotEqualTo(literal(argument)));
                }
                break;
            case GREATER_THAN:
                relations.add(relation.isGreaterThan(literal(argument)));
                break;
            case GREATER_THAN_OR_EQUAL:
                relations.add(relation.isGreaterThanOrEqualTo(literal(argument)));
                break;
            case LESS_THAN:
                relations.add(relation.isLessThan(literal(argument)));
                break;
            case LESS_THAN_OR_EQUAL:
                relations.add(relation.isLessThanOrEqualTo(literal(argument)));
                break;
            case IN:
                relations.add(relation.in(values.parallelStream()
                    .map(QueryBuilder::literal)
                    .collect(Collectors.toList())));
                break;
        }
        return relations;
    }
}
