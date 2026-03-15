package com.michal.openai.functions.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GptTool {

    private String type = "function";
    private GptFunction function;

}
