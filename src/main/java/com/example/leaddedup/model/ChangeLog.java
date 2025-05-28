package com.example.leaddedup.model;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a record of changes between an original (source) Lead and the
 * deduplicated (output) Lead.
 * 
 * This class tracks which fields changed, the cause of deduplication (by ID or
 * email),
 * and provides a formatted string representation suitable for change log
 * output.
 */
public class ChangeLog {
    private final Lead source; // The original lead before deduplication
    private final Lead output; // The lead after deduplication
    private final boolean dedupedById; // Whether deduplication was triggered by matching _id
    private final boolean dedupedByEmail; // Whether deduplication was triggered by matching email
    private final List<String> fieldChanges = new ArrayList<>(); // List of human-readable field differences

    /**
     * Constructs a ChangeLog comparing the source and output leads,
     * and records deduplication causes.
     *
     * @param source         The original lead record.
     * @param output         The retained lead record after deduplication.
     * @param dedupedById    True if deduplicated based on _id.
     * @param dedupedByEmail True if deduplicated based on email.
     */
    public ChangeLog(Lead source, Lead output, boolean dedupedById, boolean dedupedByEmail) {
        this.source = source;
        this.output = output;
        this.dedupedById = dedupedById;
        this.dedupedByEmail = dedupedByEmail;
        computeFieldChanges();
    }

    /**
     * Compares relevant fields between source and output leads,
     * populating the list of differences.
     */
    private void computeFieldChanges() {
        // Email and _id are not compared here but can be if needed
        compareField("firstName", source.getFirstName(), output.getFirstName());
        compareField("lastName", source.getLastName(), output.getLastName());
        compareField("address", source.getAddress(), output.getAddress());
        compareField("entryDate", toStr(source.getEntryDate()), toStr(output.getEntryDate()));
    }

    /**
     * Adds a formatted description of a field change if the values differ.
     *
     * @param field    The field name.
     * @param oldValue The original value.
     * @param newValue The new value.
     */
    private void compareField(String field, String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            fieldChanges.add(String.format("- %s: %s → %s", field, toDisplay(oldValue), toDisplay(newValue)));
        }
    }

    /**
     * Converts an object to a string suitable for comparison/display.
     * Returns "Unknown" if null.
     * Formats OffsetDateTime in ISO_OFFSET_DATE_TIME format.
     *
     * @param obj The object to convert.
     * @return String representation or "Unknown".
     */
    private String toStr(Object obj) {
        if (obj == null)
            return "Unknown";
        if (obj instanceof java.time.OffsetDateTime odt) {
            return odt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        return obj.toString();
    }

    /**
     * Prepares a display-friendly string for values.
     * Returns "Unknown" for null or empty strings.
     *
     * @param val The string value.
     * @return A display-ready string.
     */
    private String toDisplay(String val) {
        return (val == null || val.isEmpty()) ? "Unknown" : val;
    }

    /**
     * Formats a Lead object into a concise string representation.
     *
     * @param lead The Lead to format.
     * @return Formatted string showing key fields.
     */
    private String formatLead(Lead lead) {
        return String.format("Lead{_id='%s', email='%s', firstName='%s', lastName='%s', address='%s', entryDate=%s}",
                toDisplay(lead.getId()),
                toDisplay(lead.getEmail()),
                toDisplay(lead.getFirstName()),
                toDisplay(lead.getLastName()),
                toDisplay(lead.getAddress()),
                toStr(lead.getEntryDate()));
    }

    /**
     * Returns a formatted multi-line string summarizing
     * the source and output leads, deduplication cause,
     * and detailed field changes.
     *
     * @return Human-readable change log entry.
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Source: ").append(formatLead(source)).append("\n");
        sb.append("Output: ").append(formatLead(output)).append("\n");

        if (dedupedById && dedupedByEmail) {
            sb.append("- changes for id: ").append(toDisplay(source.getId()))
                    .append(" and email: ").append(toDisplay(source.getEmail())).append("\n");
        } else if (dedupedById) {
            sb.append("- changes for id: ").append(toDisplay(source.getId())).append("\n");
        } else if (dedupedByEmail) {
            sb.append("- changes for email: ").append(toDisplay(source.getEmail())).append("\n");
        }

        if (fieldChanges.isEmpty()) {
            sb.append("- No changes (duplicate resolved due to email only; same data kept)").append("\n");
        } else {
            for (String change : fieldChanges) {
                sb.append(change).append("\n");
            }
        }

        return sb.toString();
    }
}
