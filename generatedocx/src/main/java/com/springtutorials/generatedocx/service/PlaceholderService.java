package com.springtutorials.generatedocx.service;

import com.springtutorials.generatedocx.annotation.Placeholder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Service
public class PlaceholderService {
    public Map<String, String> createReplacementsMap(Object data) {
        Map<String, String> replacements = new HashMap<>();

        for (Field field : data.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            String placeholder = getPlaceholderName(field);
            if (placeholder != null) {
                try {
                    Object value = field.get(data);
                    replacements.put(placeholder, value != null ? value.toString() : "");
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error accessing field: " + field.getName(), e);
                }
            }
        }

        return replacements;
    }

    private String getPlaceholderName(Field field) {
        Placeholder annotation = field.getAnnotation(Placeholder.class);
        if (annotation != null) {
            String value = annotation.value();
            return value.isEmpty() ? field.getName() : value;
        }
        return null;
    }
}
