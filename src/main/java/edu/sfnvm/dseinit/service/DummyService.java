package edu.sfnvm.dseinit.service;

import edu.sfnvm.dseinit.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DummyService {
    public void controllerAdvice() throws ResourceNotFoundException {
        throw new ResourceNotFoundException("ResourceNotFoundException");
    }
}
