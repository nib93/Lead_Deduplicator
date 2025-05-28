package com.example.leaddedup.util;

import com.example.leaddedup.model.LeadWrapper;
import com.example.leaddedup.model.Lead;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.File;
import java.util.List;

/**
 * Utility class for reading lead records from a JSON file.
 * 
 * This class uses Jackson to deserialize JSON into Java objects.
 * The expected input JSON structure should wrap the list of leads
 * inside an object (e.g., { "leads": [ ... ] }).
 */
public class JsonReaderUtil {

    /**
     * Reads and parses a JSON file containing lead records.
     *
     * @param filePath The path to the JSON input file.
     * @return A List of Lead objects parsed from the file.
     * @throws IOException if the file cannot be read or parsed.
     */
    public static List<Lead> readLeadsFromFile(String filePath) throws IOException {
        // Create an ObjectMapper instance from Jackson library
        ObjectMapper mapper = new ObjectMapper();

        // Register JavaTimeModule to handle Java 8+ date/time types
        mapper.registerModule(new JavaTimeModule());

        // Disable writing dates as timestamps to preserve human-readable ISO format
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Read the input file and map it to a LeadWrapper object
        // LeadWrapper is expected to contain a list of Lead objects
        LeadWrapper wrapper = mapper.readValue(new File(filePath), LeadWrapper.class);

        // Return the list of leads extracted from the wrapper
        return wrapper.getLeads();
    }
}
