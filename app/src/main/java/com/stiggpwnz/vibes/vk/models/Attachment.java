package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.io.Serializable;

import lombok.Data;

@Data
public class Attachment implements Serializable {
    Type  type;
    Photo photo;
    Audio audio;

    public static enum Type {AUDIO, PHOTO, UNKNOWN}

    public static class TypeDeserializer extends JsonDeserializer<Type> {
        @Override
        public Type deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            switch (jsonParser.getValueAsString()) {
                case "audio":
                case "AUDIO":
                    return Type.AUDIO;
                case "photo":
                case "PHOTO":
                    return Type.PHOTO;
                default:
                    return Type.UNKNOWN;
            }
        }
    }
}
