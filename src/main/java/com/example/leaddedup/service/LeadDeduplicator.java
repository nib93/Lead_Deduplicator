package com.example.leaddedup.service;

import com.example.leaddedup.model.ChangeLog;
import com.example.leaddedup.model.Lead;

import java.util.*;

/**
 * LeadDeduplicator is responsible for processing a list of leads to remove
 * duplicates
 * based on _id and email fields. It supports two approaches for deduplication:
 * 
 * Approach 1. Map-Based Deduplication (commented out here)
 * Approach 2. Transitive Deduplication using Union-Find (Disjoint Set) data
 * structure (active implementation)
 * 
 * The deduplication prefers the lead with the latest entryDate and tracks
 * field-level changes
 * between duplicate entries for audit and logging purposes.
 */
public class LeadDeduplicator {

    // Strategy interface for choosing the preferred Lead when duplicates exist
    private final LeadComparisonStrategy strategy;

    // Maps to keep track of unique leads by id and email (email normalized to
    // lowercase)
    private final Map<String, Lead> idMap = new LinkedHashMap<>();
    private final Map<String, String> emailToIdMap = new HashMap<>();

    // List to hold leads that are considered invalid (missing id or email)
    private final List<Lead> badLeads = new ArrayList<>();

    // List to hold change logs describing how duplicates were merged/changed
    private final List<ChangeLog> changeLogs = new ArrayList<>();

    /**
     * Constructs LeadDeduplicator with a custom strategy for comparing leads.
     * 
     * @param strategy LeadComparisonStrategy instance for deciding preferred lead
     */
    public LeadDeduplicator(LeadComparisonStrategy strategy) {
        this.strategy = strategy;
    }

    // --------------------------------
    // Nested Union-Find (Disjoint Set) class for transitive deduplication
    // --------------------------------

    /**
     * Union-Find data structure to efficiently group connected leads by _id or
     * email.
     * This allows transitive duplicate detection (e.g., A matches B by email, B
     * matches C by id => A, B, C same group).
     */
    class UnionFind {
        private int[] parent;
        private int[] rank;

        /**
         * Initializes UnionFind for a given size.
         * Each element starts as its own parent (self-root).
         * 
         * @param size Number of elements (leads)
         */
        public UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++)
                parent[i] = i;
        }

        /**
         * Finds the root parent of element x with path compression.
         * 
         * @param x element index
         * @return root parent index
         */
        public int find(int x) {
            if (parent[x] != x)
                parent[x] = find(parent[x]); // Path compression
            return parent[x];
        }

        /**
         * Unions the sets containing elements x and y.
         * Uses union by rank optimization.
         * 
         * @param x element index
         * @param y element index
         */
        public void union(int x, int y) {
            int px = find(x);
            int py = find(y);
            if (px == py)
                return; // Already in the same set

            // Attach smaller rank tree under root of higher rank tree
            if (rank[px] < rank[py]) {
                parent[px] = py;
            } else if (rank[py] < rank[px]) {
                parent[py] = px;
            } else {
                parent[py] = px;
                rank[px]++;
            }
        }
    }

    /**
     * Processes the input list of leads to perform deduplication.
     * Steps:
     * 1. Filters out bad leads missing required fields (_id or email).
     * 2. Uses Union-Find to group leads transitively by _id or email.
     * 3. Deduplicates each group by choosing the preferred lead (latest entryDate).
     * 4. Logs changes between duplicates.
     * 5. Populates internal maps with the deduplicated leads.
     * 
     * @param leads List of input leads to deduplicate
     */
    public void processLeads(List<Lead> leads) {
        List<Lead> badLeads = new ArrayList<>();
        List<Lead> validLeads = new ArrayList<>();

        // 1. Filter out bad leads with missing _id or email
        for (Lead lead : leads) {
            if (lead.getId() == null || lead.getId().trim().isEmpty() ||
                    lead.getEmail() == null || lead.getEmail().trim().isEmpty()) {
                badLeads.add(lead);
            } else {
                validLeads.add(lead);
            }
        }

        if (validLeads.isEmpty()) {
            // No valid leads to process, exit early
            return;
        }

        // 2. Initialize Union-Find structure to group leads by connectedness
        int n = validLeads.size();
        UnionFind uf = new UnionFind(n);

        // Maps to remember which index corresponds to which id/email for union
        // operations
        Map<String, Integer> idToIndex = new HashMap<>();
        Map<String, Integer> emailToIndex = new HashMap<>();

        // Union leads sharing same _id or email (case-insensitive)
        for (int i = 0; i < n; i++) {
            Lead lead = validLeads.get(i);

            String id = lead.getId();
            String emailNorm = lead.getEmail().toLowerCase();

            // Union by _id if already seen
            if (idToIndex.containsKey(id)) {
                uf.union(i, idToIndex.get(id));
            } else {
                idToIndex.put(id, i);
            }

            // Union by normalized email if already seen
            if (emailToIndex.containsKey(emailNorm)) {
                uf.union(i, emailToIndex.get(emailNorm));
            } else {
                emailToIndex.put(emailNorm, i);
            }
        }

        // 3. Group leads by their root parent index from Union-Find
        Map<Integer, List<Lead>> groups = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = uf.find(i);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(validLeads.get(i));
        }

        // 4. For each group, pick preferred lead and log changes for duplicates
        for (List<Lead> group : groups.values()) {
            // Pick first as preferred initially
            Lead preferred = group.get(0);

            // Compare each lead in group to choose the preferred one based on strategy
            for (int i = 1; i < group.size(); i++) {
                preferred = strategy.choosePreferred(preferred, group.get(i));
            }

            // Log changes for all leads that differ from preferred
            for (Lead other : group) {
                if (!preferred.equals(other)) {
                    boolean dedupedById = preferred.getId().equals(other.getId());
                    boolean dedupedByEmail = preferred.getEmail().equalsIgnoreCase(other.getEmail());
                    changeLogs.add(new ChangeLog(other, preferred, dedupedById, dedupedByEmail));
                }
            }

            // Add the preferred lead to id and email maps
            idMap.put(preferred.getId(), preferred);
            emailToIdMap.put(preferred.getEmail().toLowerCase(), preferred.getId());
        }
    }

    /**
     * Returns the list of deduplicated leads after processing.
     * 
     * @return List of unique leads
     */
    public List<Lead> getDeduplicatedLeads() {
        return new ArrayList<>(idMap.values());
    }

    /**
     * Returns the list of leads considered invalid (missing _id or email).
     * 
     * @return List of invalid leads
     */
    public List<Lead> getBadLeads() {
        return badLeads;
    }

    /**
     * Returns the list of change logs detailing how duplicates were resolved.
     * 
     * @return List of ChangeLog entries
     */
    public List<ChangeLog> getChangeLogs() {
        return changeLogs;
    }

    // ----------------------
    // Approach 1: Map-Based Deduplication (Commented out)
    // ----------------------
    /**
     * This commented method shows an alternative approach that deduplicates leads
     * using simple maps keyed by _id and email, without transitive closure.
     * It chooses preferred leads by strategy and logs changes similarly.
     */
    /*
     * public void processLeads(List<Lead> leads) {
     * for (Lead lead : leads) {
     * if (lead.getId() == null || lead.getId().isBlank() ||
     * lead.getEmail() == null || lead.getEmail().isBlank()) {
     * badLeads.add(lead);
     * continue;
     * }
     * 
     * String normalizedEmail = lead.getEmail().toLowerCase();
     * boolean dedupedById = idMap.containsKey(lead.getId());
     * boolean dedupedByEmail = emailToIdMap.containsKey(normalizedEmail);
     * 
     * Lead existing = null;
     * String existingId = null;
     * 
     * if (dedupedById) {
     * existing = idMap.get(lead.getId());
     * existingId = lead.getId();
     * } else if (dedupedByEmail) {
     * existingId = emailToIdMap.get(normalizedEmail);
     * existing = idMap.get(existingId);
     * }
     * 
     * if (existing != null) {
     * Lead preferred = strategy.choosePreferred(existing, lead);
     * idMap.put(preferred.getId(), preferred);
     * emailToIdMap.put(preferred.getEmail().toLowerCase(), preferred.getId());
     * 
     * // Remove old id if replaced
     * if (!preferred.getId().equals(existingId)) {
     * idMap.remove(existingId);
     * }
     * 
     * // Log changes if fields differ
     * if (!preferred.equals(existing)) {
     * changeLogs.add(new ChangeLog(existing, preferred, dedupedById,
     * dedupedByEmail));
     * } else if (!preferred.equals(lead)) {
     * changeLogs.add(new ChangeLog(lead, preferred, dedupedById, dedupedByEmail));
     * }
     * } else {
     * idMap.put(lead.getId(), lead);
     * emailToIdMap.put(normalizedEmail, lead.getId());
     * }
     * }
     * }
     */
}
