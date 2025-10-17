package com.michal.openai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Slf4j
@Configuration
public class AppConfig {

    @Value("${CHAT_LOG_DIR}")
    private String logDir;

    @Value("${CHAT_LOG_FILENAME}")
    private String logFileName;

    public void defineLog() {

        File logDirToCreate = new File(logDir);
        if (!logDirToCreate.exists()) {
            boolean created = logDirToCreate.mkdirs();
            if (!created) {
                log.debug(" Failed to create log folder: " + logDirToCreate);
            }
        }

        log.debug("Log directory is " + logDir);

    }
}
