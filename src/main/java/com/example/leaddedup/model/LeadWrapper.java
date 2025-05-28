package com.example.leaddedup.model;

import java.util.List;

/**
 * Wrapper class to hold a list of Lead objects.
 * 
 * Used for JSON deserialization when leads are contained within an object with
 * a "leads" field.
 */
public class LeadWrapper {
    private List<Lead> leads;

    /**
     * Returns the list of leads.
     *
     * @return List of Lead objects.
     */
    public List<Lead> getLeads() {
        return leads;
    }

    /**
     * Sets the list of leads.
     *
     * @param leads List of Lead objects to set.
     */
    public void setLeads(List<Lead> leads) {
        this.leads = leads;
    }
}
