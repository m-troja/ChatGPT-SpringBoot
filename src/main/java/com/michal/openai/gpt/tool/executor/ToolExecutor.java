    package com.michal.openai.gpt.tool.executor;

    public interface ToolExecutor<T> {

        Object execute(T arguments);
    }