package com.omnixys.person;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestPersonApplication {

    public static void main(String[] args) {
        SpringApplication.from(PersonApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
