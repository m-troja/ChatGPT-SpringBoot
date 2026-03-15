package com.michal.openai.gpt.tool.factory;

import com.michal.openai.functions.entity.GptFunction;
import com.michal.openai.functions.entity.GptTool;
import com.michal.openai.gpt.tool.annotation.GptToolAnnotation;
import com.michal.openai.gpt.tool.executor.ToolExecutor;
import com.michal.openai.gpt.tool.schema.ToolSchemaGenerator;

import java.util.Map;

public class GptToolFactory {

    public static GptTool fromExecutor(ToolExecutor<?> executor) {

        var annotation =
                executor.getClass().getAnnotation(GptToolAnnotation.class);

        Class<?> argsClass = ToolExecutorUtils.getArgsClass(executor);

        Map<String, Object> parameters =
                ToolSchemaGenerator.generate(argsClass);

        var function = new GptFunction(
        annotation.name(),
        annotation.description(),
        parameters);

        return new GptTool("function", function);
    }
}