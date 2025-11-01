package com.michal.openai.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class JsonSaver {

    private final ObjectMapper objectMapper;

    private final String basePath;

    public JsonSaver(String basePath) {
        this.basePath = basePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // pretty print
    }

    public void saveResponse(Object response) {
        saveJson(response, "response");
    }

    public void saveRequest(Object request) {
        saveJson(request, "request");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveJson(Object object, String type) {
        log.debug("Saving {} json...", type);
        try {
            String folderPath = basePath + File.separator + type;
            File folder = new File(folderPath);
            if (!folder.exists()) folder.mkdirs();

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS"));
            String fileName = type + "_" + timestamp + ".json";

            File file = new File(folder, fileName);
            objectMapper.writeValue(file, object);

        } catch (IOException e) {
            log.error("Failed to save {} JSON: {}", type, e.getMessage(), e);
        }
        log.debug("Saved {} json", type);
    }
}
