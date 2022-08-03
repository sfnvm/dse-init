package edu.sfnvm.dseinit;

import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.service.io.TbktdLieuMgrIoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class PagingQueryTests {
    private final TbktdLieuMgrIoService tbktdLieuMgrIoService;

    @Autowired
    public PagingQueryTests(TbktdLieuMgrIoService tbktdLieuMgrIoService) {
        this.tbktdLieuMgrIoService = tbktdLieuMgrIoService;
    }

    @Test
    void queryAll() {
        PagingData<TbktdLieuMgr> queryRs = tbktdLieuMgrIoService.findWithoutSolrPaging(null, null, 1000);
        log.info("PagingData size {}", queryRs.getData().size());
        Assertions.assertNotEquals(0, queryRs.getData().size());
    }
}
