package config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "entities") // Сканировать entity классы
@EnableJpaRepositories(basePackages = "repositories") // Сканировать репозитории
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}