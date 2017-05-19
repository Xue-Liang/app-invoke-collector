package com.gos.monitor.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

/**
 * Created by xue on 2017-05-19.
 */
@SpringBootApplication(
        scanBasePackages = {"com.gos.monitor"},
        exclude = {MongoAutoConfiguration.class})
public class Starter {
    public static void main(String... args) {
        SpringApplication.run(Starter.class, args);
    }


}
