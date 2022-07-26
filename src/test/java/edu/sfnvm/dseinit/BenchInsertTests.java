package edu.sfnvm.dseinit;

import edu.sfnvm.dseinit.dto.enums.SaveType;
import edu.sfnvm.dseinit.model.TbktdLieuNew;
import edu.sfnvm.dseinit.service.io.TbktdLieuNewIoService;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
public class BenchInsertTests {
    private final TbktdLieuNewIoService tbktdLieuNewIoService;

    @Autowired
    public BenchInsertTests(TbktdLieuNewIoService tbktdLieuNewIoService) {
        this.tbktdLieuNewIoService = tbktdLieuNewIoService;
    }

    @Test
    void testPrepareSaveList() {
        EasyRandom easyRandom = new EasyRandom();

        tbktdLieuNewIoService.benchmarkSave(IntStream.range(0, 1000)
                .mapToObj(value -> easyRandom.nextObject(TbktdLieuNew.class))
                .peek(tbkt -> {
                    tbkt.setDsloi(new ArrayList<>());
                    tbkt.setTtctiet(new ArrayList<>());
                })
                .collect(Collectors.toList()), SaveType.PREPARED);
    }
}
