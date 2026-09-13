# Week 5 — Integration, Deployment, and Documentation Report

**Project:** Library Book Inventory Management System  
**Author:** Internship — Junior Java Developer  
**Date:** September 2026  
**Version:** 3.0.0 (Final Integrated Release)

---

## 1. Executive Summary

This report documents the final week of the Library Book Inventory Management System internship project. The primary objectives were to integrate all individual modules developed during Weeks 1 through 4 into a single, cohesive Java application, prepare the application for deployment by packaging it as an executable JAR file, and produce comprehensive developer documentation for future maintainers.

The project has evolved from a simple in-memory book management console application into a fully integrated system featuring file-based JSON persistence, robust input validation, optimized data structures with O(1) lookup performance, clean package architecture following the Single Responsibility Principle, and automated build and deployment scripts for both Windows and Unix environments. The application is now ready for handover to an operations team for deployment and long-term maintenance.

### Final Project Metrics

| Metric | Value |
|--------|-------|
| Total Java Source Files | 10 (8 main + 1 test + 1 entry point) |
| Packages | 5 (`library`, `model`, `service`, `persistence`, `exception`, `ui`) |
| Unit Test Cases | 22 (all passing) |
| Lines of Production Code | ~650 |
| Lines of Test Code | ~286 |
| Developer Documentation | ~17,600 bytes (1,500+ words) |
| Build/Deploy Scripts | 6 (build.bat, build.sh, run.bat, run.sh, deploy.bat, deploy.sh) |
| External Dependencies | 0 (runtime), 1 (JUnit 5 for testing only) |

---

## 2. Integration Process

### 2.1 Modules Integrated

The application consists of five distinct modules, each developed and refined over the course of the internship:

**Module 1 — Domain Model (`library.model`)**
The `Book` class serves as the core domain entity. It was initially a simple POJO (Weeks 1–3) and was enhanced with constructor validation, validated setters, and proper `equals()`/`hashCode()` implementation during Week 4. This module has no dependencies on any other application module, ensuring it can be reused across different layers.

**Module 2 — Business Logic (`library.service`)**
The `LibraryService` class provides all CRUD operations for the inventory. It uses a dual-index HashMap architecture (introduced in Week 4) for O(1) lookups by both book ID and ISBN. During Week 5 integration, it was enhanced to accept an optional `DataStore` dependency for persistence, while maintaining backward compatibility with the no-argument constructor for unit testing.

**Module 3 — Persistence Layer (`library.persistence`)**
This module was newly created during Week 5 to enable data persistence across application restarts. It consists of the `DataStore` interface (defining `load()` and `save()` contracts) and the `JsonDataStore` implementation that serializes book data to a human-readable JSON file. The Strategy Pattern was applied so the persistence mechanism can be swapped without modifying the service layer.

**Module 4 — Custom Exceptions (`library.exception`)**
The `BookNotFoundException` and `DuplicateBookException` classes (introduced in Week 4) provide domain-specific error handling. They replace the generic `IllegalArgumentException` that was used in earlier weeks, enabling callers to distinguish between different error scenarios programmatically.

**Module 5 — User Interface (`library.ui`)**
The `LibraryConsoleApp` class provides the interactive menu-driven console interface, while `ConsoleInputHelper` handles input validation and parsing. Both classes were extracted from the monolithic `Main.java` during Week 4's SRP refactoring. They accept their dependencies via constructor injection, making them testable and decoupled.

### 2.2 Integration Challenges and Solutions

**Challenge 1: Persistence Integration Without Breaking Existing Code**

The biggest integration challenge was adding file-based persistence to `LibraryService` without breaking the existing 22 unit tests, which all used the no-argument constructor for pure in-memory testing.

*Solution:* A two-constructor approach was adopted. The original no-argument constructor was preserved for backward compatibility and testing, while a new constructor accepting a `DataStore` parameter was added. The `persist()` method checks for null before delegating to the data store, making it a no-op in pure in-memory mode.

```java
// Backward-compatible: pure in-memory (used by tests)
public LibraryService() {
    this.dataStore = null;
}

// Week 5: with persistence
public LibraryService(DataStore dataStore) {
    this.dataStore = dataStore;
    for (Book book : dataStore.load()) {
        booksById.put(book.getId(), book);
        isbnToId.put(book.getIsbn().toLowerCase(), book.getId());
    }
}
```

**Challenge 2: JSON Serialization Without External Libraries**

To maintain the project's zero-dependency philosophy (no Gson, Jackson, or other JSON libraries), a lightweight JSON serializer/deserializer was implemented using standard Java I/O and regex-based parsing.

*Solution:* The `JsonDataStore` uses `StringBuilder` for JSON generation and a compiled regex `Pattern` for parsing. While not suitable for production-scale applications, this approach demonstrates understanding of serialization concepts and keeps the project self-contained. The code includes proper JSON escaping for special characters and handles edge cases like empty files gracefully.

**Challenge 3: Sample Data Seeding on First Launch Only**

Previous weeks always seeded sample data on startup. With persistence, this would cause duplicate key errors on the second launch.

*Solution:* The `hasBooks()` method was added to `LibraryService`, and `Main.java` checks it before seeding. Sample data is only added when the data store is empty (first launch or after data deletion).

```java
if (!library.hasBooks()) {
    seedSampleData(library);
}
```

**Challenge 4: Data File Path Configuration**

The `JsonDataStore` needed a sensible default file path while remaining configurable for different deployment environments.

*Solution:* The default constructor uses `library_data.json` in the current working directory, while a second constructor accepts a custom `Path` for production deployments where an absolute path is preferred.

### 2.3 Integration Testing

After integration, the following verification was performed:

1. **All 22 existing unit tests passed** — confirming that the persistence integration did not break any existing functionality.
2. **Manual end-to-end testing** — the application was launched, books were added, the application was terminated and restarted, and the previously added books were verified to persist correctly.
3. **Edge case testing** — the application was tested with a missing data file (auto-creates), an empty data file (handles gracefully), and a corrupted data file (logs a warning and starts fresh).

---

## 3. Application Architecture

The application follows a three-tier Layered Architecture with clear separation of concerns:

```
+---------------------------------------------------+
|                  Presentation Layer               |
|  (UI / Console)  [library.ui, library.Main]       |
+--------------------------+------------------------+
                           |
                           v
+--------------------------+------------------------+
|                   Business Logic Layer            |
|  (Services / Domain) [library.service, model]     |
+--------------------------+------------------------+
                           |
                           v
+--------------------------+------------------------+
|                   Data Access Layer               |
|  (Persistence) [library.persistence]              |
+---------------------------------------------------+
```

### Design Patterns Employed

| Pattern | Where Applied | Benefit |
|---------|---------------|---------|
| **Strategy** | `DataStore` interface + `JsonDataStore` | Persistence mechanism can be swapped (e.g., to JDBC) without modifying business logic |
| **Dependency Injection** | `Main.java` wires all dependencies via constructors | Components are loosely coupled, testable with mocks |
| **Single Responsibility** | Each class has one focused purpose | Easy to understand, modify, and test independently |
| **Optional Pattern** | `findById()` returns `Optional<Book>` | Eliminates null-checks, compiler-enforced absence handling |

---

## 4. Technologies Used

| Technology | Purpose | Version |
|------------|---------|---------|
| **Java** | Primary programming language | JDK 17+ (tested on JDK 21) |
| **JUnit Jupiter** | Unit testing framework | 5.10.2 |
| **Maven** | Build automation and dependency management | 3.x |
| **javac / jar** | Standalone compilation and JAR packaging (no Maven required) | JDK built-in |
| **Git / GitHub** | Version control and source code hosting | Latest |
| **JSON** | Data persistence format (custom serializer, no external library) | N/A |

---

## 5. Deployment Process

### 5.1 Prerequisites

- **Java Development Kit (JDK):** Version 17 or higher must be installed. The `JAVA_HOME` environment variable should point to the JDK installation, and `java`/`javac` must be available on the system `PATH`.
- **Operating System:** Windows 10/11, macOS, or Linux.
- **Disk Space:** Minimal — the entire application (including source code) is under 100 KB.

### 5.2 Building the Application

The project provides two build paths:

**Option A: Using Build Scripts (Recommended for Simplicity)**

```cmd
REM Windows
build.bat

REM Unix / macOS
chmod +x build.sh
./build.sh
```

The build script performs four steps:
1. Cleans previous build artifacts
2. Compiles all Java source files using `javac`
3. Creates an executable JAR with a proper `Main-Class` manifest entry
4. Reports the location of the generated artifact

**Option B: Using Maven**

```cmd
mvn clean package
```

This compiles the source, runs all 22 unit tests, and packages the application into `target/library-book-inventory.jar`.

### 5.3 Deploying the Application

A dedicated deployment script automates the build-and-deploy workflow:

```cmd
REM Windows — deploy to default ./release directory
deploy.bat

REM Windows — deploy to a custom directory
deploy.bat C:\apps\library

REM Unix / macOS
chmod +x deploy.sh
./deploy.sh /opt/library-app
```

The deployment script:
1. Verifies Java installation
2. Performs a clean build
3. Compiles and packages the JAR
4. Copies the artifact to the target deployment directory
5. Creates a `start.bat` (or `start.sh`) launcher script in the deployment directory

### 5.4 Running the Application

After deployment, the application can be launched using any of these methods:

```cmd
REM Using the launcher script
start.bat

REM Using the run script (from the project root)
run.bat

REM Directly via Java
java -jar library-book-inventory.jar
```

The application presents an interactive menu:
```
==========================================
     LIBRARY BOOK INVENTORY SYSTEM
==========================================

1. Add Book
2. View All Books
3. Update Book
4. Delete Book
5. Search Book
6. Exit
```

### 5.5 Production Deployment Considerations

For deploying to a production server environment, the following recommendations apply:

1. **Data File Location:** Configure an absolute path for the data file using the `JsonDataStore(Path)` constructor instead of relying on the current working directory. Example: `/var/lib/libraryapp/data.json`.
2. **File Permissions:** Ensure the executing user has read/write access to the data file directory.
3. **Process Management:** On Linux servers, use `systemd` or `supervisord` to manage the application process for automatic restarts.
4. **Logging:** In production, consider redirecting console output to log files: `java -jar library-book-inventory.jar > app.log 2>&1`.
5. **Backup Strategy:** Implement periodic backups of `library_data.json` to prevent data loss.

---

## 6. Project File Structure

```
Library_Book_Inventory_Week3/
├── pom.xml                                # Maven build configuration
├── build.bat / build.sh                   # Standalone build scripts
├── run.bat / run.sh                       # Application launcher scripts
├── deploy.bat / deploy.sh                 # Full deployment scripts
├── README.md                              # Project overview and quick start
├── Developer_Documentation.md             # Comprehensive developer guide (1,500+ words)
├── Week4_Refactoring_Report.md            # Week 4 refactoring documentation
├── Week5_Integration_Report.md            # This document
├── library_data.json                      # Runtime data file (auto-generated)
├── .gitignore                             # Git exclusion rules
│
├── src/main/java/library/
│   ├── Main.java                          # Entry point — bootstraps dependencies
│   ├── model/
│   │   └── Book.java                      # Domain model with validation
│   ├── service/
│   │   └── LibraryService.java            # CRUD operations (O(1) lookups)
│   ├── persistence/
│   │   ├── DataStore.java                 # Strategy interface for persistence
│   │   └── JsonDataStore.java             # JSON file-based persistence
│   ├── exception/
│   │   ├── BookNotFoundException.java     # Custom runtime exception
│   │   └── DuplicateBookException.java    # Custom runtime exception
│   └── ui/
│       ├── LibraryConsoleApp.java          # Menu-driven console interface
│       └── ConsoleInputHelper.java        # Input validation utilities
│
└── src/test/java/library/service/
    └── LibraryServiceTest.java            # 22 unit tests (JUnit 5)
```

---

## 7. Testing Summary

### 7.1 Test Results: 22/22 PASSED ✓

All unit tests pass successfully, verifying that the integration did not introduce any regressions.

```
├─ JUnit Jupiter
│  └─ LibraryServiceTest
│     ├─ addBook stores a valid book and it can be retrieved ✓
│     ├─ addBook rejects a duplicate ID with DuplicateBookException ✓
│     ├─ addBook rejects a duplicate ISBN even with different casing ✓
│     ├─ getAllBooks returns a defensive copy, not the live collection ✓
│     ├─ findById returns an Optional containing the matching book ✓
│     ├─ findById returns empty Optional when no book matches ✓
│     ├─ search matches by title, case-insensitively ✓
│     ├─ search matches by author, case-insensitively ✓
│     ├─ search returns an empty list, not null, when nothing matches ✓
│     ├─ Bug #2 (fixed): search with null keyword throws IAE ✓
│     ├─ updateBook overwrites every field of an existing book ✓
│     ├─ updateBook allows a book to keep its own ISBN unchanged ✓
│     ├─ updateBook rejects ISBN belonging to a different book ✓
│     ├─ updateBook throws BookNotFoundException when not found ✓
│     ├─ deleteBook removes the book from the inventory ✓
│     ├─ deleteBook throws BookNotFoundException when not found ✓
│     └─ Book model validation (Week 4 — Issue #3)
│        ├─ Book rejects a non-positive ID ✓
│        ├─ Book rejects a null or blank title ✓
│        ├─ Book rejects a null or blank author ✓
│        ├─ Book rejects a null or blank ISBN ✓
│        ├─ Book rejects an out-of-range publication year ✓
│        └─ Book equals/hashCode is based on ID ✓
```

### 7.2 Test Coverage

| Module | Test Coverage |
|--------|--------------|
| `LibraryService` (CRUD) | 16 tests — all operations + edge cases |
| `Book` (Validation) | 6 tests — constructor/setter validation + identity |
| `JsonDataStore` | Implicitly tested through integration (manual verification) |
| `ConsoleInputHelper` | Interactive I/O — tested manually |
| `LibraryConsoleApp` | Interactive UI — tested manually |

---

## 8. Future Maintenance and Scalability Strategy

### 8.1 Extending the Application

The modular architecture makes several common extensions straightforward:

**Adding a New Entity (e.g., Library Patron)**
1. Create `library.model.Patron.java` with validation.
2. Create `library.service.PatronService.java` for business logic.
3. Create or extend `DataStore` for patron persistence.
4. Add menu items in `LibraryConsoleApp` or create a separate `PatronConsoleApp`.

**Switching to Database Persistence**
1. Add a JDBC driver dependency to `pom.xml`.
2. Create `library.persistence.JdbcDataStore implements DataStore`.
3. In `Main.java`, replace `new JsonDataStore()` with `new JdbcDataStore(connectionString)`.
4. No changes needed in `LibraryService`, `Book`, or UI code.

**Adding a REST API**
1. Adopt Spring Boot or Javalin as a web framework.
2. Create `library.controller.BookController` with HTTP endpoints.
3. Inject the existing `LibraryService` — all business logic is reusable.
4. Optionally deprecate the console UI.

### 8.2 Recommended Improvements for Production

1. **Logging Framework:** Replace `System.out.println` with SLF4J + Logback for structured, configurable logging.
2. **Configuration Management:** Externalize settings (data file path, port numbers) to a `config.properties` file.
3. **Thread Safety:** For multi-threaded environments, replace `HashMap` with `ConcurrentHashMap` and add synchronization to mutation methods.
4. **Database Migration:** For large datasets exceeding available RAM, migrate from `JsonDataStore` to `JdbcDataStore` with a relational database.
5. **CI/CD Pipeline:** Configure GitHub Actions to automate build, test, and release workflows.
6. **API Documentation:** Generate Javadoc HTML from the existing comprehensive code documentation.

### 8.3 Versioning Strategy

The project follows Semantic Versioning (SemVer):
- **1.0.0** — Week 3: Initial functional release
- **2.0.0** — Week 4: Major refactoring (breaking API changes — package restructuring)
- **3.0.0** — Week 5: Final integrated release with persistence and deployment

---

## 9. Development History

| Week | Focus Area | Key Deliverables |
|------|-----------|-----------------|
| **1–2** | Core Development | `Book` model, basic `LibraryService` with ArrayList, console menu |
| **3** | Refinement & Testing | Bug fixes, 17 unit tests, ISBN validation |
| **4** | Refactoring & Optimization | HashMap O(1) optimization, package restructuring, custom exceptions, DI, Optional, validated model, 22 tests |
| **5** | Integration & Deployment | JSON persistence, DataStore interface (Strategy pattern), build/deploy scripts, executable JAR, developer documentation, final integration |

---

## 10. Conclusion

The Week 5 integration phase successfully consolidated all components developed during Weeks 1–4 into a fully functional, deployable Java application. The key achievements of this final phase include:

1. **Seamless Module Integration** — The persistence layer was integrated with the service layer using the Strategy Pattern and Dependency Injection, without breaking any existing functionality.
2. **Deployment Readiness** — The application is packaged as an executable JAR with automated build and deployment scripts for both Windows and Unix environments.
3. **Comprehensive Documentation** — Developer documentation exceeding 1,500 words covers architecture, deployment, extension guides, and maintenance strategies.
4. **Zero External Runtime Dependencies** — The application runs on any machine with JDK 17+ installed, with no additional setup required.
5. **Complete Test Suite** — All 22 unit tests pass, providing confidence in the application's correctness and reliability.

The application is now ready for handover to an operations team. Future developers will find the codebase well-documented, modularly structured, and straightforward to extend, thanks to the design patterns and clean architecture principles applied throughout the internship.

---

*End of Week 5 Integration, Deployment, and Documentation Report.*
