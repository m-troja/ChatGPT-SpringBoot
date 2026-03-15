package com.michal.openai.gpt.tool.schema;

import com.michal.openai.gpt.tool.annotation.ToolParamAnnotation;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ToolSchemaGenerator {

    public static Map<String, Object> generate(Class<?> argsClass) {

        Map<String, Object> schema = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();

        for (Field field : argsClass.getDeclaredFields()) {

            var annotation = field.getAnnotation(ToolParamAnnotation.class);

            if (annotation == null) continue;

            Map<String, Object> property = new HashMap<>();

            property.put("type", mapType(field.getType()));
            property.put("description", annotation.description());

            properties.put(field.getName(), property);

            if (annotation.isRequired())
            {
                required.add(field.getName());
            }
        }

        if (!required.isEmpty()) {
            schema.put("required", required);
        }

        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);

        return schema;
    }

    private static String mapType(Class<?> type) {

        if (type == String.class) return "string";
        if (type == Integer.class || type == int.class) return "integer";
        if (type == Boolean.class || type == boolean.class) return "boolean";

        return "string";
    }
}
