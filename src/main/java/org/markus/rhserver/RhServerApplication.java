package org.markus.rhserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class RhServerApplication {

     static void main(String[] args) {
        SpringApplication.run(RhServerApplication.class, args);
    }
}
