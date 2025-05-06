package com.gentlecorp.person;

import org.springframework.boot.SpringApplication;

public class TestPersonApplication {

    public static void main(String[] args) {
        SpringApplication.from(PersonApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
