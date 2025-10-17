package com.michal.openai;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.michal.openai.config.AppConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EntityScan("com.michal.openai.entity") 
@EnableJpaRepositories("com.michal.openai.persistence") 
@EnableAsync(proxyTargetClass = true)
@SpringBootApplication
@Configuration
@PropertySources({
	@PropertySource("classpath:application.properties") ,  
	@PropertySource("classpath:secrets.properties")
	})
		
public class ChatGptIntegrationApplication {

    private final AppConfig appConfig;

    public ChatGptIntegrationApplication(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @PostConstruct
    public void defineLogs() {
        appConfig.defineLog();
    }
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ChatGptIntegrationApplication.class);

        // Read env var (fallback to default 8080)
        String serverAddress = System.getenv().getOrDefault("CHAT_SERVER_ADDRESS", "0.0.0.0");
        String serverPort = System.getenv().getOrDefault("CHAT_SERVER_PORT", "6969");
        String dbName = System.getenv().getOrDefault("CHAT_DB_NAME", "chatgpt-integration");
        String dbHost = System.getenv().getOrDefault("CHAT_DB_HOST", "localhost");
        String dbPort = System.getenv().getOrDefault("CHAT_DB_PORT", "5432");
        String dbUrl = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);
        String dbUsername = System.getenv().getOrDefault("CHAT_DB_USERNAME", "chatgpt");
        String dbPassword = System.getenv().getOrDefault("CHAT_DB_PASSWORD", "chatgptPW");

        // Set server.port dynamically
        Map<String, Object> props = new HashMap<>();
        props.put("server.address", serverAddress);
        props.put("server.port", serverPort);
        props.put("spring.datasource.url", dbUrl);
        props.put("spring.datasource.username", dbUsername);
        props.put("spring.datasource.password", dbPassword);
        app.setDefaultProperties(props);

        // Env var log loop
        for (String key: props.keySet()) {
            log.debug("Env var check: {} = {}", key, props.get(key));
            System.out.println("Env var check:   " + key + " = " + props.get(key));
        }
        app.run(args);
    }

}
