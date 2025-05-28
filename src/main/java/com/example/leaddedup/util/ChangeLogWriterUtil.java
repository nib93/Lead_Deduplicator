package com.example.leaddedup.util;

import com.example.leaddedup.model.ChangeLog;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for writing a list of ChangeLog entries to a plain text file.
 * 
 * Each ChangeLog entry contains details about differences between source and
 * output leads,
 * including field-level changes and duplication reasons.
 */
public class ChangeLogWriterUtil {

    /**
     * Writes the list of ChangeLog entries to a specified text file.
     *
     * @param changeLogs The list of ChangeLog entries to write.
     * @param filePath   The path to the output log file.
     * @throws IOException If writing to the file fails.
     */
    public static void writeChangeLogsToFile(List<ChangeLog> changeLogs, String filePath) throws IOException {
        // Try-with-resources to ensure writer is closed automatically
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write each formatted change log entry on a new line
            for (ChangeLog log : changeLogs) {
                writer.write(log.format()); // format() returns a human-readable string
                writer.newLine(); // Separate entries by line
            }
        }
    }
}
