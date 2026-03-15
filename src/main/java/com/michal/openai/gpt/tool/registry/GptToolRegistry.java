package com.michal.openai.gpt.tool.registry;

import com.michal.openai.functions.entity.GptTool;
import com.michal.openai.gpt.tool.annotation.GptToolAnnotation;
import com.michal.openai.gpt.tool.executor.ToolExecutor;
import com.michal.openai.gpt.tool.factory.GptToolFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GptToolRegistry {

    private final Map<String, ToolExecutor<?>> tools;

    public GptToolRegistry(List<ToolExecutor<?>> executors, Environment env) {

        // TODO REMOVE DEBUG

        log.debug("=== Listing all environment variables ===");
        System.getenv().forEach((k,v) -> {
            if (k.startsWith("CHAT_ALLOW_FUNCTION")) {
                log.debug("{}={}", k, v);
            }
        });

        log.debug("=== Listing all executors ===");
        executors.forEach(e -> {
            var ann = e.getClass().getAnnotation(GptToolAnnotation.class);
            log.debug("{} annotation={}", e.getClass().getSimpleName(), ann != null ? ann.name() : "null");
        });

        this.tools = executors.stream()
                .filter(this::isEnabled)
                .collect(Collectors.toMap(
                        e -> e.getClass().getAnnotation(GptToolAnnotation.class).name(),
                        e -> e
                ));
    }

    private boolean isEnabled(ToolExecutor<?> executor) {
        var annotation = executor.getClass().getAnnotation(GptToolAnnotation.class);
        if (annotation == null) return false;

        String envVarName = "CHAT_ALLOW_FUNCTION_" + annotation.name().toUpperCase();
        String value = System.getenv(envVarName);
        return "1".equals(value);
    }

    public ToolExecutor<?> get(String name) {
        return tools.get(name);
    }

    public List<GptTool> allGptTools() {
        return tools.values().stream().map(GptToolFactory::fromExecutor).toList();
    }

    public List<GptTool> allAllowedGptTools() {
        return tools.values().stream()
                .filter(this::isEnabled)
                .map(GptToolFactory::fromExecutor)
                .toList();
    }

}
