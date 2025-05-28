package com.example.leaddedup.util;

import com.example.leaddedup.model.Lead;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for writing a list of leads to a JSON output file.
 * 
 * This utility wraps the list inside an object with a "leads" key to maintain
 * consistent JSON structure. It also formats the output for readability and
 * ensures
 * proper serialization of date/time fields.
 */
public class JsonWriterUtil {

    /**
     * Writes a list of leads to a specified JSON file.
     *
     * @param leads    The list of Lead objects to write.
     * @param filePath The path to the output file.
     * @throws IOException If an error occurs during file writing.
     */
    public static void writeLeadsToFile(List<Lead> leads, String filePath) throws IOException {
        // Create an ObjectMapper instance for JSON serialization
        ObjectMapper mapper = new ObjectMapper();

        // Register module to support Java 8+ date/time (e.g., LocalDateTime)
        mapper.registerModule(new JavaTimeModule());

        // Disable writing dates as timestamps to preserve ISO-8601 date format
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Enable pretty-printing for better readability of the output file
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Wrap the list of leads in a map with "leads" as the key
        // This keeps the JSON structure consistent with the input
        Map<String, List<Lead>> wrapper = new HashMap<>();
        wrapper.put("leads", leads);

        // Write the wrapped leads to the specified file
        mapper.writeValue(new File(filePath), wrapper);
    }
}
