package edu.sfnvm.dseinit;

import com.google.common.io.Resources;
import edu.sfnvm.dseinit.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class CompareRunnerTests {
    @Test
    void compareTest() {
        String pathStr = "/static/tmp/migrate/conditions";

        List<String> runnedPathStr = Arrays.asList(
                "/static/tmp/migrate/conditions-01",
                "/static/tmp/migrate/conditions-02",
                "/static/tmp/migrate/conditions-03",
                "/static/tmp/migrate/conditions-04",
                "/static/tmp/migrate/conditions-05",
                "/static/tmp/migrate/conditions-06",
                "/static/tmp/migrate/conditions-07",
                "/static/tmp/migrate/conditions-08",
                "/static/tmp/migrate/conditions-09-01",
                "/static/tmp/migrate/conditions-09-02",
                "/static/tmp/migrate/conditions-09-03",
                "/static/tmp/migrate/conditions-09-04",
                "/static/tmp/migrate/conditions-09-05",
                "/static/tmp/migrate/conditions-10-01",
                "/static/tmp/migrate/conditions-10-02",
                "/static/tmp/migrate/conditions-10-03",
                "/static/tmp/migrate/conditions-10-04",
                "/static/tmp/migrate/conditions-10-05-01",
                "/static/tmp/migrate/conditions-10-05-02",
                "/static/tmp/migrate/conditions-10-05-03",
                "/static/tmp/migrate/conditions-10-05-04",
                "/static/tmp/migrate/conditions-10-05-05"
        );

        Set<Pair<String, Instant>> originSet;
        Set<Pair<String, Instant>> runnedList = new HashSet<>();

        try {
            for (String str : runnedPathStr) {
                URL path = getClass().getResource(str);
                runnedList.addAll(Arrays.stream(Resources.toString(
                        Objects.requireNonNull(path),
                        StandardCharsets.UTF_8).split("\n"))
                    .filter(s -> StringUtils.hasLength(s) && !s.startsWith("#"))
                    .map(s -> s.split(";"))
                    .filter(strings -> strings.length == 2)
                    .map(split -> new Pair<>(split[0], DateUtil.parseStringToUtcInstant(split[1])))
                    .collect(Collectors.toSet()));
            }

            URL path = getClass().getResource(pathStr);
            originSet = Arrays.stream(Resources.toString(
                    Objects.requireNonNull(path),
                    StandardCharsets.UTF_8).split("\n"))
                .filter(s -> StringUtils.hasLength(s) && !s.startsWith("#"))
                .map(s -> s.split(";"))
                .filter(strings -> strings.length == 2)
                .map(split -> new Pair<>(split[0], DateUtil.parseStringToUtcInstant(split[1])))
                .collect(Collectors.toSet());

            log.info("originSet size {}", originSet.size());
            log.info("runnedList size {}", runnedList.size());
            runnedList.forEach(p -> {
                if (!originSet.contains(p)) {
                    log.error("originSet NOT contain {}", p);
                } else {
                    log.info("originSet contain {}", p);
                }
            });
        } catch (Exception e) {
            log.warn(
                "Cannot find 'to-delete' file with path '{}'\n{}",
                pathStr,
                e.getMessage());
        }
    }
}
