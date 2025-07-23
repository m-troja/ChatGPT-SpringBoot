package com.michal.openai;
import java.io.File;

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

	
    public static void main(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();
        String logPath = os.contains("windows") ? "C:/tmp/log" : "/var/log";

        // set log_path into env variable
        System.setProperty("LOG_PATH", logPath);
        
        // Tworzenie katalogu jeśli nie istnieje
        File logDir = new File(logPath);
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (!created) {
                System.err.println(" Failed to create log folder: " + logPath);
            }
        }
        
        log.info("App started!");


        
        SpringApplication.run(ChatGptIntegrationApplication.class, args);
    }
}
