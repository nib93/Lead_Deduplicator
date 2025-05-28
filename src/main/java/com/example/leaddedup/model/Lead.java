package com.example.leaddedup.model;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.example.leaddedup.util.CustomOffsetDateTimeDeserializer;

/**
 * Model class representing a Lead entity.
 * 
 * Annotated for JSON serialization/deserialization using Jackson.
 * Fields include id, email, personal details, address, and entry date.
 */
@JsonPropertyOrder({ "_id", "email", "firstName", "lastName", "address", "entryDate" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Lead {
    @JsonProperty("_id")
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String address;

    @JsonDeserialize(using = CustomOffsetDateTimeDeserializer.class)
    private OffsetDateTime entryDate;

    /**
     * Parameterized constructor for all fields.
     */
    public Lead(String id, String email, String firstName, String lastName, String address, OffsetDateTime entryDate) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.entryDate = entryDate;
    }

    /**
     * Default no-argument constructor for Jackson.
     */
    public Lead() {
    }

    // Getters and setters for all fields

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public OffsetDateTime getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(OffsetDateTime entryDate) {
        this.entryDate = entryDate;
    }

    /**
     * Returns true if the lead has both a non-null, non-empty _id and email.
     * Used to determine if the lead is valid for processing.
     *
     * @return true if both _id and email are present and non-empty, false
     *         otherwise.
     */
    @JsonIgnore
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() && email != null && !email.trim().isEmpty();
    }
}
