package com.fruit.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fruit.annotation.Sensitive;
import com.fruit.annotation.SensitiveStrategy;

import java.io.IOException;

public class SensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private SensitiveStrategy strategy;

    public SensitiveSerializer() {}

    public SensitiveSerializer(SensitiveStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null && strategy != null) {
            gen.writeString(strategy.desensitizer().apply(value));
        } else {
            gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            Sensitive annotation = property.getAnnotation(Sensitive.class);
            if (annotation == null) {
                annotation = property.getContextAnnotation(Sensitive.class);
            }
            if (annotation != null) {
                return new SensitiveSerializer(annotation.strategy());
            }
        }
        return this;
    }
}
