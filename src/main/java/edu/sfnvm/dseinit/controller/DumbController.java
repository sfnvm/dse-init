package edu.sfnvm.dseinit.controller;

import edu.sfnvm.dseinit.dto.PagingData;
import edu.sfnvm.dseinit.dto.TbOneDto;
import edu.sfnvm.dseinit.exception.ResourceNotFoundException;
import edu.sfnvm.dseinit.mapper.TbOneMapper;
import edu.sfnvm.dseinit.model.TbOne;
import edu.sfnvm.dseinit.service.io.TbOneIoService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("dumb")
public class DumbController {
    private final TbOneIoService tbOneIoService;
    private final TbOneMapper tbOneMapper = Mappers.getMapper(TbOneMapper.class);

    @Autowired
    public DumbController(TbOneIoService tbOneIoService) {
        this.tbOneIoService = tbOneIoService;
    }

    @GetMapping("{id}")
    public ResponseEntity<TbOneDto> findByPartitionKeys(@PathVariable UUID id)
    throws ResourceNotFoundException {
        TbOne queryResult = tbOneIoService
                .findByPartition(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found"));
        return ResponseEntity.ok(tbOneMapper.map(queryResult));
    }

    @PostMapping("search")
    public ResponseEntity<PagingData<TbOne>> searchByRawQuery(@RequestBody Map<String, String> mapBody) {
        return ResponseEntity.ok(tbOneIoService.findByRawQuery(
                mapBody.getOrDefault("query", "select * from sfnvm.t_1"),
                mapBody.getOrDefault("state", null),
                100)
        );
    }
}
