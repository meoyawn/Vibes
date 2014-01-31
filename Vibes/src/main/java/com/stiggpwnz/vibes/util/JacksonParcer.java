package com.stiggpwnz.vibes.util;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;

import flow.Parcer;

/**
 * Created by adel on 1/31/14
 */
public class JacksonParcer<T> implements Parcer<T> {

    final ObjectMapper objectMapper;

    public JacksonParcer(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    @Override
    public Parcelable wrap(T instance) {
        try {
            String json = encode(instance);
            return new Wrapper(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T unwrap(Parcelable parcelable) {
        Wrapper wrapper = (Wrapper) parcelable;
        try {
            return decode(wrapper.json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String encode(T instance) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonGenerator generator = objectMapper.getFactory().createGenerator(stringWriter);

        try {
            generator.writeStartObject();
            generator.writeFieldName(instance.getClass().getName());
            generator.writeObject(instance);
            generator.writeEndObject();

            return stringWriter.toString();
        } finally {
            generator.close();
            stringWriter.close();
        }
    }

    private T decode(String json) throws IOException {
        JsonParser parser = objectMapper.getFactory().createParser(json);

        try {
            parser.nextToken();
            parser.nextToken();
            parser.nextToken();

            Class<?> clazz = Class.forName(parser.getCurrentName());
            return (T) parser.readValueAs(clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            parser.close();
        }
    }

    private static class Wrapper implements Parcelable {

        public static final Parcelable.Creator<Wrapper> CREATOR = new Parcelable.Creator<Wrapper>() {
            @Override
            public Wrapper createFromParcel(Parcel in) {
                String json = in.readString();
                return new Wrapper(json);
            }

            @Override
            public Wrapper[] newArray(int size) { return new Wrapper[size]; }
        };

        final String json;

        Wrapper(String json) { this.json = json; }

        @Override
        public int describeContents() { return 0; }

        @Override
        public void writeToParcel(Parcel out, int flags) { out.writeString(json); }
    }
}
