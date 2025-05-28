package com.example.leaddedup.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom deserializer for parsing OffsetDateTime fields in JSON.
 * 
 * This is needed when the default Jackson parser doesn't match the specific
 * date format used in your JSON (e.g., "2023-07-15T10:30:00-07:00").
 */
public class CustomOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    // Define the expected date-time format pattern with timezone offset
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    /**
     * Parses a string date into an OffsetDateTime object.
     * 
     * @param p    The JSON parser providing the string value.
     * @param ctxt The deserialization context (unused).
     * @return The parsed OffsetDateTime or null if the input is blank.
     * @throws IOException If the date string cannot be parsed.
     */
    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText(); // Get the raw text value from JSON
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null; // Handle empty or missing date strings gracefully
        }
        return OffsetDateTime.parse(dateStr, FORMATTER); // Parse using custom formatter
    }
}
