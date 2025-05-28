# Overview
## **Problem Statement:**
This Java-based deduplication tool is designed to intelligently process a variable number of JSON lead records—whether clean or messy—identify duplicates by \_id or email, and output a conflict-free, up-to-date dataset with a detailed change log.\
The deduplication follows a defined set of rules:

- Duplicates are identified using the \_id and/or email fields (with case-insensitive comparison for email).
- When duplicates are found, the record with the most recent entryDate is retained.
- If entryDate values are identical, the record that appears last in the input list is preferred.
- All changes made during the reconciliation process are recorded in a human-readable change\_log.txt, showing:
- The source record (replaced record),
- The output record (retained final record),
- And a field-level diff (e.g., firstName: John → Jane).

## Tech Stack & Libraries:
- **Java 17** - Primary language
- **Jackson 2.15.2** - For JSON parsing (ObjectMapper, JavaTimeModule)
- **JUnit 5** - Unit testing framework
- **Maven** - Build and dependency management
- 
## Project Setup, Installation & Running
### Prerequisites
- JDK 17+
- Maven 3.8+
## **Input:** 
`	`The tool accepts a single JSON file input containing an array of Lead entries, specified as a command line argument when running the program.

There are two examples input files:

**leads.json** *(src/main/resources/data/leads.json)*— Contains well-formed leads with no missing or null required fields.
**leadsModified.json** *(src/main/resources/data/leadsModified.json)* — Contains leads with some missing, null, or empty fields, including invalid leads.

Each lead contains:

- \_id (required)
- email (required)
- firstName (optional)
- lastName (optional)
- address (optional)
- entryDate (optional, ISO 8601 string or empty)

**Important:**\
Leads missing or having null/empty \_id or email are considered **bad entries** and will be written separately to BadJson.json.
## **Output:**
- **dedupedleads.json**  *(src/main/resources/data/dedupedLeads.json)*: The list of valid, deduplicated leads. It has JSON array of deduplicated, valid leads (after merging duplicates).
- **changeLog.txt** *(src/main/resources/data/changeLog.txt):* Field-level differences for merged leads. It has Human-readable text describing all field-level changes due to deduplication.
- **BadJson.json** *(src/main/resources/data/BadJson.json):* Leads missing required fields (\_id or email). It has Contains leads with some missing, null, or empty fields, including invalid leads.
## **Assumptions:** 
`  `**leads.json :** 

- All records are identically structured and structurally valid JSON objects.
- Each record contains **all six expected fields**: \_id, email, firstName, lastName, address, entryDate
- No fields are missing or null.
- entryDate is either a valid ISO 8601 string (e.g., "2023-11-15T11:21:18Z") or an empty string.
- Email comparison is **case-insensitive**.
- BadJson.json will be **empty** for this input because all records are valid.
- Deduplication is tested purely based on \_id and/or email collisions.
- The file serves as the ideal input for testing core deduplication functionality without edge case interference.
- changeLog.txt must track:
- Field-level differences between retained and discarded leads.
- No memory constraint for JSON input data.

**leadsModified.json:** 

- Records are **well-formed JSON**, but some may have:
  - **Missing fields** (e.g., no entryDate, email, or \_id etc.)
  - **Null or empty field values** (e.g., "\_id": "", "email": null)
- \_id and email are mandatory:
  - If either \_id or email is missing or empty (""), the entry is considered bad entry and written to BadJson.json.
- Other fields (firstName, lastName, address, entryDate) can be:
  - Missing or null → treated as "Unknown" during comparison for changeLog.txt
- For valid entries:
  - firstName, lastName, and address may be present but contain "" (empty string).
  - entryDate may be null, missing, or empty (""). In such cases:
    - The lead with a non-empty entryDate is preferred during deduplication.
    - If both are missing entryDate, a consistent fallback (e.g., first entry) is selected.
- Email comparison is **case-insensitive**.
- When resolving duplicates:
  - Leads with valid entryDate take precedence.
  - If all entryDate values are null/missing/empty, pick any lead consistently.
- changeLog.txt must track:
  - Field-level differences between retained and discarded leads.
  - Which fields were “Unknown” (null/missing) vs. actual values.
- The file is ideal for testing:
  - Bad entry filtering
  - Merging incomplete leads
  - Logging field-level differences
  - Transitive duplicate resolution

## Design Specification

For complete technical details, algorithm explanation, input/output formats, and assumptions, Classes and Methods details refer to the design PDF:


**[Design_Document.pdf])**
(https://github.com/user-attachments/files/20472019/Design.Document.pdf)

## Algorithm Analysis: 

Folloing document represents two methods for deduplicating lead data: a simple map-based approach and a more robust union-find-based method. It explains their logic, algorithms, pros and cons, and includes sample inputs to demonstrate how duplicates are detected and resolved.

- **Approach 1**: Simple Map-Based Deduplication (commented in LeadDuplicator.java)
- **Approach 2**: Transitive Deduplication using Union-Find (Disjoint Set) (Implemented)
 **See full write-up, Comparision of the two approaches in Detail **: [Dedup Approaches  Analysis.pdf](https://github.com/user-attachments/files/20472026/Dedup.Approaches.Analysis.pdf)


## Installation and Setup Project

**Steps**

\# Clone repo

$ git clone <repo-url>

$ cd lead-deduplication

\# Run tests

$ mvn test

\# Build project

$ mvn clean compile

\# Run:  leads.json

mvn exec:java -Dexec.mainClass="com.example.leaddedup.Main" -Dexec.args="src/main/resources/data/leads.json"

\# Run:  leadsModified.json

mvn exec:java -Dexec.mainClass="com.example.leaddedup.Main" -Dexec.args="src/main/resources/data/leadsModified.json”

### Complexity Analysis
----------------------

#### Time Complexity

*   **Parsing JSON**: O(n)
    
*   **Validation**: O(n)
    
*   **Union-Find operations** (with path compression): ~O(α(n)) per operation ⇒ O(n) overall
    
*   **Merging & Logging**: O(n \* m) where m = number of fields
    
*   **Total**: **O(n × m)**
    

#### Space Complexity

*   **Parsed Data Storage**: O(n)
    
*   **Union-Find Structures**: O(n)
    
*   **Change Logs & Outputs**: O(n)
    
*   **Total**: **O(n)**

## Author

By Neeti Ishan Bhatt 
