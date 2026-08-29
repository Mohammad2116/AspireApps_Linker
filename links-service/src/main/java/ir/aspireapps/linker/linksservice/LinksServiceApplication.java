package ir.aspireapps.linker.linksservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@EnableCaching
public class LinksServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinksServiceApplication.class, args);
    }

}
