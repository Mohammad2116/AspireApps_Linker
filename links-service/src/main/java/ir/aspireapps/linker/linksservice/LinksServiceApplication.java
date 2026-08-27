package ir.aspireapps.linker.linksservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LinksServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinksServiceApplication.class, args);
    }

}
