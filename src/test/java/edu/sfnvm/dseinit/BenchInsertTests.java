package edu.sfnvm.dseinit;

import com.google.common.collect.Lists;
import edu.sfnvm.dseinit.dto.StateTimeoutDto;
import edu.sfnvm.dseinit.dto.enums.SaveType;
import edu.sfnvm.dseinit.exception.CustomException;
import edu.sfnvm.dseinit.model.TbktdLieuNew;
import edu.sfnvm.dseinit.service.io.CacheIoService;
import edu.sfnvm.dseinit.service.io.TbktdLieuNewIoService;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
public class BenchInsertTests {
    private final TbktdLieuNewIoService tbktdLieuNewIoService;
    private final CacheIoService cacheIoService;
    private static final int CHUNK_SIZE = 30000;

    @Autowired
    public BenchInsertTests(
            TbktdLieuNewIoService tbktdLieuNewIoService,
            CacheIoService cacheIoService) {
        this.tbktdLieuNewIoService = tbktdLieuNewIoService;
        this.cacheIoService = cacheIoService;
    }

    @Test
    void saveTest() throws InterruptedException, CustomException {
        StopWatch sw = new StopWatch();

        List<List<TbktdLieuNew>> chunks = Lists.partition(buildList(CHUNK_SIZE), CHUNK_SIZE / 3);
        if (CollectionUtils.isEmpty(chunks) && chunks.size() != 3) {
            throw new CustomException("Invalid input");
        }

        sw.start("SIMPLE");
        log.info("Start {}", sw.currentTaskName());
        // tbktdLieuNewIoService.benchmarkSave(buildList(CHUNK_SIZE), SaveType.SIMPLE);
        sw.stop();

        sw.start("PREPARED");
        log.info("Start {}", sw.currentTaskName());
        // tbktdLieuNewIoService.benchmarkSave(buildList(CHUNK_SIZE), SaveType.PREPARED);
        sw.stop();

        sw.start("ASYNC");
        log.info("Start {}", sw.currentTaskName());
        tbktdLieuNewIoService.benchmarkSave(buildList(CHUNK_SIZE), SaveType.ASYNC);
        sw.stop();

        Thread.sleep(10000);

        StopWatch.TaskInfo[] listofTasks = sw.getTaskInfo();
        for (StopWatch.TaskInfo task : listofTasks) {
            log.info("[{}]:[{}]", task.getTaskName(), task.getTimeMillis());
        }

        log.info(sw.prettyPrint());

        List<TbktdLieuNew> cacheMgr = cacheIoService.getMgrTimeoutCache();
        List<StateTimeoutDto> cacheState = cacheIoService.getStateTimeoutCache();
        log.info("cacheIoService.getMgrTimeoutCache() size: {}", cacheMgr.size());
        log.info("cacheIoService.getStateTimeoutCache() size: {}", cacheState.size());
        Assertions.assertTrue(CollectionUtils.isEmpty(cacheMgr));
        Assertions.assertTrue(CollectionUtils.isEmpty(cacheState));
    }

    @SuppressWarnings("SameParameterValue")
    private List<TbktdLieuNew> buildList(int size) {
        EasyRandom er = new EasyRandom();
        return IntStream.range(0, size).mapToObj(i -> {
            TbktdLieuNew obj = er.nextObject(TbktdLieuNew.class);
            obj.setDsloi(new ArrayList<>());
            obj.setTtctiet(new ArrayList<>());
            return obj;
        }).collect(Collectors.toList());
    }
}
