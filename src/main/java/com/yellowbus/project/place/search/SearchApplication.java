package com.yellowbus.project.place.search;

import com.yellowbus.project.place.search.component.QueueConsumer;
import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
@AllArgsConstructor
public class SearchApplication {

    private static QueueConsumer queueConsumer;

    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
        new Thread(queueConsumer).start();
    }

}
