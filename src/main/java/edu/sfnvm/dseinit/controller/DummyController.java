package edu.sfnvm.dseinit.controller;

import edu.sfnvm.dseinit.exception.ResourceNotFoundException;
import edu.sfnvm.dseinit.service.DummyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("dummies")
public class DummyController {
  private final DummyService dummyService;

  @Autowired
  public DummyController(DummyService dummyService) {
    this.dummyService = dummyService;
  }

  @GetMapping("controller-advice")
  public ResponseEntity<?> controllerAdvice() throws ResourceNotFoundException {
    dummyService.controllerAdvice();
    return ResponseEntity.ok().build();
  }
}
