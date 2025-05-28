package com.example.leaddedup.service;

import com.example.leaddedup.model.Lead;

/**
 * An interface for defining deduplication strategies.
 * 
 * Implementations decide how to select the preferred lead when duplicates are
 * found.
 * This allows for customizable deduplication logic such as latest-entry-wins or
 * custom scoring.
 */
public interface LeadComparisonStrategy {

    /**
     * Chooses the preferred lead between two duplicate entries.
     *
     * @param lead1 The first duplicate lead.
     * @param lead2 The second duplicate lead.
     * @return The lead that should be retained.
     */
    Lead choosePreferred(Lead lead1, Lead lead2);
}
