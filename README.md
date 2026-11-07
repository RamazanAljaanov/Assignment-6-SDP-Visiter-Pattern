# Assignment 06: Visitor Pattern — Advanced File System

**Course:** ShP-2216 | Software Design Patterns  
**Programme:** 6B06102 | Software Engineering — Astana IT University  
**Format:** Individual | Java 17 | Maven  

---

## 1. Project Overview

This project implements an **in-memory hierarchical file system** utilizing the **Visitor behavioral pattern** with **Double Dispatch**. It allows three independent operations to execute over the file system composite structure without modifying the element hierarchy or performing runtime type discrimination (`instanceof` or reflection).

### Supported Operations (Visitors)
1. **`TotalSizeVisitor` (R3):** Computes the recursive aggregate byte size of all files in a subtree. Directories do not add size.
2. **`SearchVisitor` (R4):** Searches for files meeting a filename criterion and an inclusive minimum-size threshold (`size >= minSize`). Outputs deterministic, root-relative paths separated by `/` and sorted lexicographically.
3. **`StatisticsVisitor` (R5, R8):** Gathers total file count, total directory count (including root if it is a directory), and maximum tree depth (`root depth = 0`). Implemented purely as an extension without modifying existing elements.

---

## 2. Architecture & Design Principles

### Double Dispatch Sequence
The Visitor pattern eliminates `if-instanceof` type checking by employing two polymorphic dispatches:
1. **First dispatch:** Polymorphic invocation of `element.accept(visitor)` resolves to the concrete element runtime type (`FileNode` or `DirectoryNode`).
2. **Second dispatch:** Inside `accept()`, `visitor.visit(this)` resolves to the overloaded visit method on the concrete visitor (`visit(FileNode)` or `visit(DirectoryNode)`).

### Traversal Ownership (R7)
**Traversal Owner:** The **Visitors** own the traversal over child collections.  
*Justification:* By placing child iteration inside `visit(DirectoryNode)`, the element classes (`DirectoryNode`) remain strictly passive domain models unburdened with operation-specific navigation state (e.g., path stack in `SearchVisitor`, depth level in `StatisticsVisitor`). This preserves the Single Responsibility Principle.

### Tree Integrity Invariants (R6)
* **No Negative File Sizes:** Validated at construction; throws `IllegalArgumentException`.
* **No Duplicate Sibling Names:** Enforced on `addChild()`; throws `IllegalArgumentException`.
* **Single Parent Ownership:** Validated on `addChild()`; throws `IllegalStateException`.
* **Cycle Prevention:** Ancestor chain check using reference equality (`==`) without `instanceof`; throws `IllegalStateException`.
* **Encapsulation:** Child elements returned as `Collections.unmodifiableList()`.

---

## 3. Project Structure

```text
kz.aitu.sdp.filesystem/
├── model/
│   ├── FileSystemElement.java     # Base Element interface with accept()
│   ├── FileNode.java              # Concrete file (name, size >= 0)
│   └── DirectoryNode.java         # Composite directory (children, integrity checks)
├── visitor/
│   ├── FileSystemVisitor.java     # Visitor interface with visit(FileNode) & visit(DirectoryNode)
│   ├── TotalSizeVisitor.java      # Subtree size accumulator
│   ├── SearchVisitor.java         # Filtered root-relative lexicographical search
│   └── StatisticsVisitor.java     # Metrics: file count, dir count, max depth
└── Main.java                      # Deterministic demonstration app
```

---

## 4. How to Build, Test, and Run

### Prerequisites
* **Java 17+** (Compatible with OpenJDK 17, 21, 25)
* **Apache Maven 3.8+**

### Running Automated Verification (JUnit 5)
To execute all 8 unit tests (covering checkpoints T1 through T8):

```bash
mvn clean test
```

### Running the Demonstration Application
To execute the console demonstration:

```bash
mvn compile exec:java
```

Alternatively, run via plain `java`:
```bash
mvn package
java -cp target/classes kz.aitu.sdp.filesystem.Main
```

---

## 5. Automated Verification Checklist (T1 - T8)

| Check | Target | Status |
| :--- | :--- | :---: |
| **T1** | Both element types reach the correct overloaded `visit()` methods via double dispatch. | PASSED |
| **T2** | Size accumulation across multiple nesting levels is correct. | PASSED |
| **T3** | Search correctly combines both configurable filter conditions. | PASSED |
| **T4** | Search produces correct, deterministic, root-relative paths sorted lexicographically. | PASSED |
| **T5** | Statistics are correct for nesting, counting, and maximum depth. | PASSED |
| **T6** | Empty structures and no-match search cases are handled predictably. | PASSED |
| **T7** | Repeat executions do not leak or multiply accumulated state. | PASSED |
| **T8** | Invalid tree mutations are rejected while valid nesting remains supported. | PASSED |

---

## 6. Clean Code Justifications

1. **Meaningful Naming (`TotalSizeVisitor`, `DirectoryNode.addChild`):** Intent-revealing identifiers immediately communicate behavior and domain semantics without requiring explanatory comments.
2. **Single Responsibility Principle (`FileNode`, `SearchVisitor`):** `FileNode` is solely responsible for file attributes, while `SearchVisitor` isolates the search and path-formatting algorithm.
3. **Small Methods (`DirectoryNode.addChild`, `buildCurrentPath`):** Each method focuses on a single task with minimal lines of code and low cognitive complexity.
4. **Encapsulation (`DirectoryNode.getChildren`):** Returns an unmodifiable list wrapper, completely preventing external modification of internal child collections.
5. **Elimination of Type-Checking Chains (Double Dispatch in `accept`):** Avoids fragile `if (node instanceof FileNode)` chains, making operations open for extension and closed for modification (Open-Closed Principle).

---

## 7. Visitor Pattern Trade-Offs

* **Advantage (Adding Operations):** Introducing new operations (like `StatisticsVisitor`) is trivial and adheres to the Open-Closed Principle (OCP). Zero changes were made to existing element classes.
* **Trade-Off (Adding Element Types):** Introducing a new element type (e.g., `SymlinkNode`) violates OCP across all visitors: every existing visitor interface and implementation must be modified to add a new `visit(SymlinkNode)` method.
