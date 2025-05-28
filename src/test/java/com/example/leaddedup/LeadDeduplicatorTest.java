package com.example.leaddedup;

import com.example.leaddedup.model.ChangeLog;
import com.example.leaddedup.model.Lead;
import com.example.leaddedup.service.LeadDeduplicator;
import com.example.leaddedup.service.LatestEntryWinsStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LeadDeduplicatorTest {
  private LeadDeduplicator deduplicator;

  /**
   * Set up the LeadDeduplicator instance before each test,
   * using the LatestEntryWinsStrategy for deduplication.
   */
  @BeforeEach
  public void setUp() {
    deduplicator = new LeadDeduplicator(new LatestEntryWinsStrategy());
  }

  /**
   * Helper method to create a Lead instance with the specified parameters.
   * Parses entryDate from ISO-8601 string or sets null if missing.
   */
  private Lead createLead(String id, String email, String firstName, String lastName, String address,
      String entryDateStr) {
    OffsetDateTime entryDate = entryDateStr != null ? OffsetDateTime.parse(entryDateStr) : null;
    Lead lead = new Lead();
    lead.setId(id);
    lead.setEmail(email);
    lead.setFirstName(firstName);
    lead.setLastName(lastName);
    lead.setAddress(address);
    lead.setEntryDate(entryDate);
    return lead;
  }

  // ===========================
  // Deduplication Logic Tests
  // ===========================

  /**
   * Tests deduplication when two leads have the same email.
   * The lead with the later entryDate should win.
   */
  @Test
  public void testSimpleDeduplication() {
    Lead lead1 = createLead("1", "test@example.com", "John", "Doe", "123 St", "2024-01-01T10:00:00Z");
    Lead lead2 = createLead("2", "test@example.com", "John", "Doe", "123 St", "2024-01-02T10:00:00Z");
    deduplicator.processLeads(List.of(lead1, lead2));
    List<Lead> result = deduplicator.getDeduplicatedLeads();
    assertEquals("2", result.get(0).getId());
  }

  /**
   * Tests deduplication when two leads have the same _id but different emails.
   * The lead with the later entryDate should win and update email accordingly.
   */

  @Test
  public void testDuplicateById() {
    Lead lead1 = createLead("1", "a@example.com", "A", "X", "A St", "2023-01-01T00:00:00Z");
    Lead lead2 = createLead("1", "b@example.com", "B", "Y", "B St", "2023-02-01T00:00:00Z");
    deduplicator.processLeads(List.of(lead1, lead2));
    List<Lead> result = deduplicator.getDeduplicatedLeads();
    assertEquals("b@example.com", result.get(0).getEmail());
  }

  /**
   * Tests transitive deduplication: duplicates connected by _id and email.
   * Leads a and c share the same id, b and a share the same email.
   * All should be deduplicated into one lead with latest entryDate.
   */

  @Test
  public void testTransitiveDuplicatesByIdAndEmail() {
    Lead a = createLead("1", "x@example.com", "A", "X", "Addr1", "2024-01-01T00:00:00Z");
    Lead b = createLead("2", "x@example.com", "B", "Y", "Addr2", "2024-01-02T00:00:00Z");
    Lead c = createLead("1", "z@example.com", "C", "Z", "Addr3", "2024-01-03T00:00:00Z");
    deduplicator.processLeads(List.of(a, b, c));
    List<Lead> result = deduplicator.getDeduplicatedLeads();
    assertEquals("C", result.get(0).getFirstName());
  }

  /**
   * Tests deduplication when multiple leads share the same email.
   * The one with the latest entryDate should be retained.
   */

  @Test
  public void testMultipleDuplicates() {
    List<Lead> leads = new ArrayList<>();
    leads.add(createLead("1", "same@example.com", "A", "X", "1 St", "2023-01-01T00:00:00Z"));
    leads.add(createLead("2", "same@example.com", "B", "Y", "2 St", "2023-03-01T00:00:00Z"));
    leads.add(createLead("3", "same@example.com", "C", "Z", "3 St", "2023-02-01T00:00:00Z"));
    deduplicator.processLeads(leads);
    List<Lead> result = deduplicator.getDeduplicatedLeads();
    assertEquals("B", result.get(0).getFirstName());
  }

  /**
   * Tests behavior when no duplicates are present.
   * Both leads should be retained.
   */
  @Test
  public void testNoDuplicates() {
    Lead l1 = createLead("1", "a@example.com", "A", "X", "A St", "2023-01-01T00:00:00Z");
    Lead l2 = createLead("2", "b@example.com", "B", "Y", "B St", "2023-01-01T00:00:00Z");
    deduplicator.processLeads(List.of(l1, l2));
    List<Lead> result = deduplicator.getDeduplicatedLeads();
    assertEquals(2, result.size());
  }

  /**
   * Tests that email comparison is case-insensitive during deduplication.
   */
  @Test
  public void testCaseInsensitiveEmail() {
    Lead l1 = createLead("1", "TEST@EXAMPLE.COM", "A", "X", "St", "2023-01-01T00:00:00Z");
    Lead l2 = createLead("2", "test@example.com", "B", "Y", "St", "2023-02-01T00:00:00Z");
    deduplicator.processLeads(List.of(l1, l2));
    List<Lead> result = deduplicator.getDeduplicatedLeads();
    assertEquals("B", result.get(0).getFirstName());
  }

  /**
   * Tests that the latest entryDate wins even if newer lead has missing fields.
   */
  @Test
  public void testPreferNonEmptyEntryDate() {
    Lead l1 = createLead("1", "a@example.com", "A", "X", "Addr", null);
    Lead l2 = createLead("2", "a@example.com", "B", "Y", "Addr", "2023-01-01T00:00:00Z");
    deduplicator.processLeads(List.of(l1, l2));
    List<Lead> result = deduplicator.getDeduplicatedLeads();
    assertEquals("B", result.get(0).getFirstName());
  }

  /**
   * Tests that the latest entryDate wins even if newer lead has missing fields.
   */
  @Test
  public void testLatestEntryDateWinsEvenIfMissingFields() {
    Lead l1 = createLead("1", "a@example.com", "A", "X", "Addr", "2023-01-01T00:00:00Z");
    Lead l2 = createLead("2", "a@example.com", null, null, null, "2024-01-01T00:00:00Z");
    deduplicator.processLeads(List.of(l1, l2));
    List<Lead> result = deduplicator.getDeduplicatedLeads();
    assertNull(result.get(0).getFirstName());
  }

  // ================================
  // Bad JSON Input Handling Tests
  // ================================

  /**
   * Tests that leads with missing _id are excluded from output.
   */
  @Test
  public void testBadLeadMissingId() {
    Lead l = createLead(null, "test@example.com", "A", "B", "Addr", "2023-01-01T00:00:00Z");
    deduplicator.processLeads(List.of(l));
    List<Lead> result = deduplicator.getDeduplicatedLeads();
    assertTrue(result.isEmpty());
  }

  /**
   * Tests that leads with missing email are excluded from output.
   */

  @Test
  public void testBadLeadMissingEmail() {
    Lead l = createLead("1", null, "A", "B", "Addr", "2023-01-01T00:00:00Z");
    deduplicator.processLeads(List.of(l));
    List<Lead> result = deduplicator.getDeduplicatedLeads();
    assertTrue(result.isEmpty());
  }

  /**
   * Tests that leads with empty _id are excluded from output.
   */
  @Test
  public void testBadLeadEmptyId() {
    Lead l = createLead("", "test@example.com", "A", "B", "Addr", "2023-01-01T00:00:00Z");
    deduplicator.processLeads(List.of(l));
    List<Lead> result = deduplicator.getDeduplicatedLeads();
    assertTrue(result.isEmpty());
  }

  /**
   * Tests that leads with empty email are excluded from output.
   */
  @Test
  public void testBadLeadEmptyEmail() {
    Lead l = createLead("1", "", "A", "B", "Addr", "2023-01-01T00:00:00Z");
    deduplicator.processLeads(List.of(l));
    List<Lead> result = deduplicator.getDeduplicatedLeads();
    assertTrue(result.isEmpty());
  }

  // ===================================
  // Change Log Accuracy and Edge Cases
  // ===================================

  /**
   * Tests that a single field change is logged correctly.
   */
  @Test
  public void testChangeLogSingleFieldChange() {
    Lead lead1 = createLead("1", "a@example.com", "John", "Doe", "123 St", "2024-01-01T10:00:00Z");
    Lead lead2 = createLead("1", "a@example.com", "John", "Smith", "123 St", "2024-01-02T10:00:00Z");

    List<Lead> leads = List.of(lead1, lead2);
    deduplicator.processLeads(leads);

    List<Lead> deduped = deduplicator.getDeduplicatedLeads();

    List<ChangeLog> logs = deduplicator.getChangeLogs();

    assertEquals(1, deduped.size());
    assertEquals("1", deduped.get(0).getId());
    assertEquals(1, logs.size());

    ChangeLog log = logs.get(0);
    assertTrue(log.format().contains("- lastName: Doe → Smith"));
  }

  /**
   * Tests that multiple fields changes are logged correctly.
   */
  @Test
  public void testChangeLogMultipleFields() {
    Lead lead1 = createLead("1", "a@example.com", "John", "Doe", "123 St", "2024-01-01T10:00:00Z");
    Lead lead2 = createLead("1", "a@example.com", "Jane", "Smith", "456 Ave", "2024-01-02T10:00:00Z");

    List<Lead> leads = List.of(lead1, lead2);
    deduplicator.processLeads(leads);
    List<ChangeLog> logs = deduplicator.getChangeLogs();

    ChangeLog log = logs.get(0);
    String formatted = log.format();
    assertTrue(formatted.contains("- firstName: John → Jane"));
    assertTrue(formatted.contains("- lastName: Doe → Smith"));
    assertTrue(formatted.contains("- address: 123 St → 456 Ave"));
  }

  /**
   * Tests that when the newer lead entry has missing (null) fields,
   * the change log correctly records these fields as changed from their previous
   * values
   * to "Unknown" in the output, reflecting the loss of information.
   */
  @Test
  public void testChangeLogMissingFieldInOlderEntry() {
    Lead lead1 = createLead("1", "a@example.com", null, null, null, "2024-01-01T10:00:00Z");
    Lead lead2 = createLead("1", "a@example.com", "Jane", "Smith", "456 Ave", "2024-01-02T10:00:00Z");

    deduplicator.processLeads(List.of(lead1, lead2));
    ChangeLog log = deduplicator.getChangeLogs().get(0);
    String formatted = log.format();

    assertTrue(formatted.contains("- firstName: Unknown → Jane"));
    assertTrue(formatted.contains("- lastName: Unknown → Smith"));
    assertTrue(formatted.contains("- address: Unknown → 456 Ave"));
  }

  /**
   * Tests that when a newer lead entry has missing (null) values for certain
   * fields,
   * the deduplication process logs the change correctly by showing the original
   * values
   * being replaced with "Unknown" to indicate missing data in the newer record.
   */
  @Test
  public void testChangeLogMissingFieldInNewerEntry() {
    Lead lead1 = createLead("1", "a@example.com", "John", "Doe", "123 St", "2024-01-01T10:00:00Z");
    Lead lead2 = createLead("1", "a@example.com", null, null, null, "2024-01-02T10:00:00Z");

    deduplicator.processLeads(List.of(lead1, lead2));
    ChangeLog log = deduplicator.getChangeLogs().get(0);
    String formatted = log.format();

    assertTrue(formatted.contains("- firstName: John → Unknown"));
    assertTrue(formatted.contains("- lastName: Doe → Unknown"));
    assertTrue(formatted.contains("- address: 123 St → Unknown"));
  }

  /**
   * Verifies the deduplication behavior when two leads have the same entryDate
   * but all other fields differ. Ensures that at least one change log entry
   * is generated according to the strategy implemented for tie-breaking.
   */
  @Test
  public void testAllFieldsDifferentSameDate() {
    Lead lead1 = createLead("1", "a@example.com", "John", "Doe", "123 St", "2024-01-01T10:00:00Z");
    Lead lead2 = createLead("1", "b@example.com", "Jane", "Smith", "456 Ave", "2024-01-01T10:00:00Z");

    deduplicator.processLeads(List.of(lead1, lead2));
    List<ChangeLog> logs = deduplicator.getChangeLogs();

    // Because entryDates equal, prefer first or implement your strategy, at least
    // one log expected
    assertFalse(logs.isEmpty());
  }

  /**
   * Tests deduplication when one lead has null entryDate and another has valid
   * entryDate.
   */
  @Test
  public void testNullEntryDateTreatedAsEmpty() {
    Lead lead1 = createLead("1", "a@example.com", "John", "Doe", "123 St", null);
    Lead lead2 = createLead("1", "a@example.com", "John", "Smith", "123 St", "2024-01-01T10:00:00Z");

    deduplicator.processLeads(List.of(lead1, lead2));
    List<Lead> deduped = deduplicator.getDeduplicatedLeads();
    assertEquals("Smith", deduped.get(0).getLastName());
  }

  // 21. Ensures a future-dated lead wins in deduplication
  @Test
  public void testFutureEntryDateWins() {
    Lead lead1 = createLead("1", "a@example.com", "John", "Doe", "123 St", "2024-01-01T10:00:00Z");
    Lead lead2 = createLead("1", "a@example.com", "John", "Smith", "123 St", "2030-01-01T10:00:00Z");
    deduplicator.processLeads(List.of(lead1, lead2));
    List<Lead> deduped = deduplicator.getDeduplicatedLeads();
    assertEquals("Smith", deduped.get(0).getLastName());
  }

  // 22. Validates behavior with an empty input; output also empty
  @Test
  public void testEmptyInputList() {
    deduplicator.processLeads(List.of());
    List<Lead> deduped = deduplicator.getDeduplicatedLeads();
    assertTrue(deduped.isEmpty());
    assertTrue(deduplicator.getChangeLogs().isEmpty());
  }
}
