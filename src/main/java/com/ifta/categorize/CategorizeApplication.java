package com.ifta.categorize;

import com.ifta.categorize.config.MongoConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@SpringBootApplication
@Import({MongoConfig.class})
public class CategorizeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CategorizeApplication.class, args);
    }
}
