package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment implements Serializable {

    private Type  type;
    private Photo photo;
    private Audio audio;
    private Link  link;

    public Photo getPhoto() { return photo; }

    public void setPhoto(Photo photo) { this.photo = photo; }

    public Audio getAudio() { return audio; }

    public void setAudio(Audio audio) { this.audio = audio; }

    public Link getLink() { return link; }

    public void setLink(Link link) { this.link = link; }

    public Type getType() { return type; }

    @JsonDeserialize(using = TypeDeserializer.class)
    public void setType(Type type) { this.type = type; }

    public static enum Type {
        AUDIO,
        PHOTO,
        UNKNOWN
    }

    static class TypeDeserializer extends JsonDeserializer<Type> {
        @Override
        public Type deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            switch (jsonParser.getValueAsString()) {
                case "audio": return Type.AUDIO;
                case "photo": return Type.PHOTO;
                default: return Type.UNKNOWN;
            }
        }
    }
}
