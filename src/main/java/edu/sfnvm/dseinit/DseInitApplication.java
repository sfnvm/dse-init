package edu.sfnvm.dseinit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@EnableCaching
public class DseInitApplication {
    public static void main(String[] args) {
        SpringApplication.run(DseInitApplication.class, args);
    }
}
