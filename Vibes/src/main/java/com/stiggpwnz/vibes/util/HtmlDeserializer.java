package com.stiggpwnz.vibes.util;

import android.text.Html;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Created by adel on 11/9/13
 */
public class HtmlDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return Html.fromHtml(parser.getValueAsString()).toString();
    }
}
