package market.example.easy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EasyApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyApplication.class, args);
    }
}