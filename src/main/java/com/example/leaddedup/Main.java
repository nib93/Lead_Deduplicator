package com.example.leaddedup;

import com.example.leaddedup.model.Lead;
import com.example.leaddedup.service.LeadDeduplicator;
import com.example.leaddedup.service.LatestEntryWinsStrategy;
import com.example.leaddedup.util.ChangeLogWriterUtil;
import com.example.leaddedup.util.JsonReaderUtil;
import com.example.leaddedup.util.JsonWriterUtil;

import java.util.List;

/**
 * Main application class for lead deduplication.
 *
 * Usage: java -jar lead-deduplication.jar inputFile.json
 * Output files: dedupedLeads.json, changeLog.txt, BadJson.json
 */
public class Main {
    public static void main(String[] args) {
        // Ensure the input file is provided via command line arguments
        if (args.length < 1) {
            System.err.println(
                    "Usage Command for leads.json : mvn exec:java -Dexec.mainClass=\"com.example.leaddedup.Main\" -Dexec.args=\"src/main/resources/data/leads.json\" \n Use Command for leadsModified.json mvn exec:java -Dexec.mainClass=\"com.example.leaddedup.Main\" -Dexec.args=\"src/main/resources/data/leads.json\" ");

            System.exit(1);
        }

        // Input file path is the first argument
        String inputFilePath = args[0];

        // Output file paths (relative to project root or resources folder)
        String outputFilePath = "src/main/resources/data/dedupedLeads.json";
        String badJsonFilePath = "src/main/resources/data/BadJson.json";
        String changeLogPath = "src/main/resources/data/changeLog.txt";

        try {
            // Read and parse input JSON leads into a list of Lead objects
            List<Lead> inputLeads = JsonReaderUtil.readLeadsFromFile(inputFilePath);

            // Instantiate the deduplicator with the chosen strategy (latest entry wins)
            LeadDeduplicator deduplicator = new LeadDeduplicator(new LatestEntryWinsStrategy());

            // Process input leads: remove duplicates, validate malformed entries, and
            // collect change logs
            deduplicator.processLeads(inputLeads);

            // Write deduplicated leads to output JSON
            JsonWriterUtil.writeLeadsToFile(deduplicator.getDeduplicatedLeads(), outputFilePath);

            // Write malformed or bad leads to a separate JSON file
            JsonWriterUtil.writeLeadsToFile(deduplicator.getBadLeads(), badJsonFilePath);

            // Write detailed field-level change logs to a text file
            ChangeLogWriterUtil.writeChangeLogsToFile(deduplicator.getChangeLogs(), changeLogPath);

            // Notify success
            System.out.println("Deduplication completed successfully.");
        } catch (Exception e) {
            // Print any exceptions that occur during processing
            System.err.println("Error processing leads: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
