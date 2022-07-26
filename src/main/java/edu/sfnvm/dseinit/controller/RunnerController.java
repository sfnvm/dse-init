package edu.sfnvm.dseinit.controller;

import edu.sfnvm.dseinit.dto.StateTimeoutDto;
import edu.sfnvm.dseinit.dto.enums.SaveType;
import edu.sfnvm.dseinit.model.TbktdLieuNew;
import edu.sfnvm.dseinit.service.RetryService;
import edu.sfnvm.dseinit.service.RunnerService;
import edu.sfnvm.dseinit.service.io.CacheIoService;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("runners")
public class RunnerController {
    private final RunnerService runnerService;
    private final RetryService retryService;
    private final CacheIoService cacheIoService;

    @Autowired
    public RunnerController(
            RunnerService runnerService,
            RetryService retryService,
            CacheIoService cacheIoService) {
        this.runnerService = runnerService;
        this.retryService = retryService;
        this.cacheIoService = cacheIoService;
    }

    @GetMapping("caches/retry")
    public ResponseEntity<Response> retryCaches() {
        retryService.retryCached(SaveType.ASYNC);
        return ResponseEntity.ok(Response.builder().status("Running").build());
    }


    /**
     * <h2>Mgr</h2>
     */
    @GetMapping("caches/mgr")
    public ResponseEntity<List<TbktdLieuNew>> getMgrTimeoutCache() {
        return ResponseEntity.ok(cacheIoService.getMgrTimeoutCache());
    }

    @PostMapping("caches/mgr")
    public ResponseEntity<TbktdLieuNew> postMgrTimeoutCache(
            @RequestBody TbktdLieuNew tbktdLieuNew
    ) {
        return ResponseEntity.ok(runnerService.putMgrTimeoutCache(tbktdLieuNew));
    }

    @DeleteMapping("caches/mgr")
    public ResponseEntity<Void> clearMgrTimeoutCache() {
        runnerService.clearMgrTimeoutCache();
        return ResponseEntity.ok().build();
    }


    /**
     * <h2>State</h2>
     */
    @GetMapping("caches/state")
    public ResponseEntity<List<StateTimeoutDto>> getStateTimeoutCache() {
        return ResponseEntity.ok(cacheIoService.getStateTimeoutCache());
    }

    @PostMapping("caches/state")
    public ResponseEntity<StateTimeoutDto> postStateTimeoutCache(@RequestBody StateTimeoutDto dto) {
        return ResponseEntity.ok(runnerService.putStateTimeoutCache(dto));
    }

    @DeleteMapping("caches/state")
    public ResponseEntity<Void> clearStateTimeoutCache() {
        runnerService.clearStateTimeoutCache();
        return ResponseEntity.ok().build();
    }

    @Data
    @Builder
    public static class PairDto {
        private String value0;
        private Integer value1;
    }

    @Data
    @Builder
    public static class Response {
        private String status;
    }
}
