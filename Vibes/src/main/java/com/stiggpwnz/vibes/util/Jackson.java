package com.stiggpwnz.vibes.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.MimeUtil;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class Jackson {

	private static class Holder {
		private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	}

	public static ObjectMapper getObjectMapper() {
		return Holder.OBJECT_MAPPER;
	}

	public static class JacksonConverter implements Converter {

		private final ObjectMapper objectMapper;

		public JacksonConverter(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
		}

		@Override
		public Object fromBody(TypedInput body, Type type) throws ConversionException {
			String charset = "UTF-8";
			if (body.mimeType() != null) {
				charset = MimeUtil.parseCharset(body.mimeType());
			}
			InputStreamReader isr = null;
			try {
				isr = new InputStreamReader(body.in(), charset);
				return objectMapper.readValue(isr, TypeFactory.rawClass(type));
			} catch (IOException e) {
				throw new ConversionException(e);
			} finally {
				if (isr != null) {
					try {
						isr.close();
					} catch (IOException ignored) {
					}
				}
			}
		}

		@Override
		public TypedOutput toBody(Object object) {
			try {
				return new JsonTypedOutput(objectMapper.writeValueAsBytes(object));
			} catch (JsonProcessingException e) {
				throw new AssertionError(e);
			}
		}

		private static class JsonTypedOutput implements TypedOutput {

			private final byte[] jsonBytes;

			public JsonTypedOutput(byte[] jsonBytes) {
				this.jsonBytes = jsonBytes;
			}

			@Override
			public String fileName() {
				return null;
			}

			@Override
			public String mimeType() {
				return "application/json; charset=UTF-8";
			}

			@Override
			public long length() {
				return jsonBytes.length;
			}

			@Override
			public void writeTo(OutputStream out) throws IOException {
				out.write(jsonBytes);
			}
		}
	}
}
