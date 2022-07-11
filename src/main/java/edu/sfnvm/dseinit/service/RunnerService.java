package edu.sfnvm.dseinit.service;

import com.google.common.io.Resources;
import edu.sfnvm.dseinit.mapper.TbktdLieuNewMapper;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.model.TbktdLieuNew;
import edu.sfnvm.dseinit.service.io.TbktdLieuMgrIoService;
import edu.sfnvm.dseinit.service.io.TbktdLieuNewIoService;
import edu.sfnvm.dseinit.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RunnerService implements ApplicationRunner {
    private final TbktdLieuMgrIoService tbktDLieuMgrIoService;
    private final TbktdLieuNewIoService tbktDLieuNewIoService;

    private final TbktdLieuNewMapper mapper = Mappers.getMapper(TbktdLieuNewMapper.class);

    @Autowired
    public RunnerService(
            TbktdLieuMgrIoService tbktDLieuMgrIoService,
            TbktdLieuNewIoService tbktDLieuNewIoService) {
        this.tbktDLieuMgrIoService = tbktDLieuMgrIoService;
        this.tbktDLieuNewIoService = tbktDLieuNewIoService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Instant start = Instant.now();
        log.info("Mannual audit data started at: {}", start);

        // List<String> tinList = Arrays.stream(Resources.toString(
        // 				Objects.requireNonNull(getClass().getResource("/static/tin")),
        // 				StandardCharsets.UTF_8).split("\n"))
        // 		.filter(StringUtils::hasText)
        // 		.collect(Collectors.toList());
        // List<Instant> instantList = Arrays.stream(Resources.toString(
        // 				Objects.requireNonNull(getClass().getResource("/static/time")),
        // 				StandardCharsets.UTF_8).split("\n"))
        // 		.filter(StringUtils::hasText)
        // 		.map(DateUtil::parseStringToUtcInstant)
        // 		.collect(Collectors.toList());
        // List<Pair<String, Instant>> conditions = tinList.stream()
        // 		.flatMap(s -> instantList.stream().map(d -> new Pair<>(s, d)))
        // 		.collect(Collectors.toList());

        List<Pair<String, Instant>> conditions = Arrays.stream(Resources.toString(
                        Objects.requireNonNull(getClass().getResource("/static/conditions")),
                        StandardCharsets.UTF_8).split("\n"))
                .filter(StringUtils::hasText)
                .map(s -> {
                    String[] split = s.split(";");
                    return new Pair<>(split[0], DateUtil.parseStringToUtcInstant(split[1]));
                }).collect(Collectors.toList());

        Map<Pair<String, Instant>, List<TbktdLieuMgr>> result =
                tbktDLieuMgrIoService.findAndTransformByPartitionKeys(conditions);

        result.values().forEach(tbktDLieuMgrs -> {
            for (int i = 0; i < tbktDLieuMgrs.size(); i++) {
                TbktdLieuNew toInsert = builder(tbktDLieuMgrs.get(i), i, ChronoUnit.MILLIS);
                tbktDLieuNewIoService.saveAsync(toInsert);
            }
        });

        log.info("Mannual audit data done at: {}", Duration.between(start, Instant.now()));
    }

    private TbktdLieuNew builder(TbktdLieuMgr sourceData, long incrementValue, ChronoUnit unitType) {
        TbktdLieuNew result = mapper.map(sourceData);
        result.setNtao(sourceData.getNtao().plus(incrementValue, unitType));
        result.setId(UUID.randomUUID());
        return result;
    }
}
