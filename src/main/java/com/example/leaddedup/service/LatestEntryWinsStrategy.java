package com.example.leaddedup.service;

import com.example.leaddedup.model.Lead;

import java.time.OffsetDateTime;

/**
 * A deduplication strategy that chooses the lead with the latest (most recent)
 * entryDate.
 * 
 * If both leads have null entry dates, the first one is chosen by default.
 * If only one has a valid entry date, that one is chosen.
 * If both have valid entry dates, the one with the more recent date is
 * selected.
 */
public class LatestEntryWinsStrategy implements LeadComparisonStrategy {

    /**
     * Compares two leads and returns the one considered more recent/preferred.
     *
     * @param lead1 The first lead candidate.
     * @param lead2 The second lead candidate.
     * @return The preferred lead based on entryDate.
     */
    @Override
    public Lead choosePreferred(Lead lead1, Lead lead2) {
        OffsetDateTime date1 = lead1.getEntryDate();
        OffsetDateTime date2 = lead2.getEntryDate();

        // If both dates are null, fall back to keeping the first lead
        if (date1 == null && date2 == null) {
            return lead1;
        }
        // If only one lead has a date, prefer the one with a valid date
        else if (date1 == null) {
            return lead2;
        } else if (date2 == null) {
            return lead1;
        }
        // If both dates are present, return the more recent one
        else {
            return date1.isAfter(date2) ? lead1 : lead2;
        }
    }
}
