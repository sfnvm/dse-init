package edu.sfnvm.dseinit.controller;

import edu.sfnvm.dseinit.model.TbktdLieuNew;
import edu.sfnvm.dseinit.service.RunnerService;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("runner")
public class RunnerController {
    private final RunnerService runnerService;

    @Autowired
    public RunnerController(RunnerService runnerService) {
        this.runnerService = runnerService;
    }

    @GetMapping("caches/retry")
    public ResponseEntity<Response> retryCaches() {
        runnerService.retryCached();
        return ResponseEntity.ok(Response.builder().status("Running").build());
    }

    @GetMapping("caches")
    public ResponseEntity<List<TbktdLieuNew>> getCachedValues() {
        return ResponseEntity.ok(runnerService.getCache());
    }

    @PostMapping("caches")
    public ResponseEntity<TbktdLieuNew> postCache(@RequestBody TbktdLieuNew tbktdLieuNew) {
        return ResponseEntity.ok(runnerService.putCache(tbktdLieuNew));
    }

    @DeleteMapping("caches")
    public ResponseEntity<Void> clearCache() {
        runnerService.clearCache();
        return ResponseEntity.ok().build();
    }

    @Data
    @Builder
    public static class Response {
        private String status;
    }
}
