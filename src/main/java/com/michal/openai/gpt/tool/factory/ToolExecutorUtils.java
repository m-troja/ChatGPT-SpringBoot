package com.michal.openai.gpt.tool.factory;

import com.michal.openai.gpt.tool.executor.ToolExecutor;

import java.lang.reflect.ParameterizedType;

public class ToolExecutorUtils {

    public static Class<?> getArgsClass(ToolExecutor<?> executor) {

        ParameterizedType type =
                (ParameterizedType) executor.getClass().getGenericInterfaces()[0];

        return (Class<?>) type.getActualTypeArguments()[0];
    }
}