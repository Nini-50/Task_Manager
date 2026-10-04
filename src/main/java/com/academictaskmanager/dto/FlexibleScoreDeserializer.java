package com.academictaskmanager.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * ESPN represents a competitor's score differently depending on the endpoint: the scoreboard API
 * returns a plain string (e.g. {@code "20"}), while the team-schedule API returns an object
 * (e.g. {@code {"value": 20.0, "displayValue": "20"}}). This deserializer normalizes both shapes
 * to a plain display string so {@link EspnCompetitorDto} works against either response.
 */
public class FlexibleScoreDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            if (node.hasNonNull("displayValue")) {
                return node.get("displayValue").asText();
            }
            if (node.hasNonNull("value")) {
                return node.get("value").asText();
            }
            return null;
        }
        return node.asText();
    }
}
