package edu.sfnvm.dseinit.h2.service;

import edu.sfnvm.dseinit.h2.model.TbktdlCondition;
import edu.sfnvm.dseinit.h2.repository.TbktdlConditionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TbktdlConditionService {
    private final TbktdlConditionRepository tbktdlConditionRepository;

    @Autowired
    public TbktdlConditionService(TbktdlConditionRepository tbktdlConditionRepository) {
        this.tbktdlConditionRepository = tbktdlConditionRepository;
    }

    public void initData() {
        log.info("Condition service initializing data .....");

        // TODO: Read data from file

        List<TbktdlCondition> tbktdlConditions = new ArrayList<>();

        // TODO: Delete
        tbktdlConditions.add(TbktdlCondition.builder()
            .mst("1111111111")
            .id(UUID.randomUUID())
            .ntao(Instant.now())
            .build());

        // TODO: Delete
        tbktdlConditions.add(TbktdlCondition.builder()
            .mst("2222222222")
            .id(UUID.randomUUID())
            .ntao(Instant.now())
            .build());

        tbktdlConditionRepository.saveAll(tbktdlConditions);
        log.info("Condition service done init data, total record inserted: {}", tbktdlConditions.size());
    }
}
