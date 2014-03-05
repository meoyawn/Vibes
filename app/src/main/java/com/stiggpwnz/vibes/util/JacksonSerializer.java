package com.stiggpwnz.vibes.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import de.devland.esperandro.serialization.Serializer;
import timber.log.Timber;

@Singleton
public class JacksonSerializer implements Serializer {
    final Lazy<ObjectMapper> objectMapper;

    @Inject
    public JacksonSerializer(Lazy<ObjectMapper> objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(Object object) {
        try {
            return objectMapper.get().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            Timber.e(e, "exception while serializing");
            throw new RuntimeException(e);
        }
    }

    @Override
    public String serialize(Serializable serializable) {
        try {
            return objectMapper.get().writeValueAsString(serializable);
        } catch (JsonProcessingException e) {
            Timber.e(e, "exception while serializing");
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.get().readValue(json, clazz);
        } catch (Exception e) {
            Timber.e(e, "exception while deserializing");
            throw new RuntimeException(e);
        }
    }

    public <T> T deserialize(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.get().readValue(json, typeReference);
        } catch (Exception e) {
            Timber.e(e, "exception while deserializing");
            throw new RuntimeException(e);
        }
    }
}
