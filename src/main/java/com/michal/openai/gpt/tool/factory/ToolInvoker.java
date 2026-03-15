package com.michal.openai.gpt.tool.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.gpt.tool.executor.ToolExecutor;
import org.springframework.stereotype.Component;

@Component
public class ToolInvoker {

    private final ObjectMapper mapper;

    public ToolInvoker(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Object invoke(ToolExecutor executor, String jsonArgs) {

        Class<?> argsClass = ToolExecutorUtils.getArgsClass(executor);

        try {

            Object args = mapper.readValue(jsonArgs, argsClass);

            return executor.execute(args);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
