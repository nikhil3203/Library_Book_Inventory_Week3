# Developer Documentation: Library Book Inventory Management System

## 1. Application Overview
The Library Book Inventory Management System is a robust, terminal-based Java application designed to facilitate the efficient management of a library's book collection. Its primary purpose is to provide librarians and library administrators with an intuitive interface to add, search, update, view, and delete book records. 

Targeted primarily at small to medium-sized libraries or as an educational archetype for enterprise Java development, the application emphasizes clean architecture, maintainability, and data integrity. It enforces business rules (such as unique ISBNs and valid publication years) and persists data automatically to prevent loss. By acting as a foundational system, it is built with extensibility in mind, allowing future developers to easily swap the console UI for a web interface or the file-based storage for a relational database.

## 2. Architecture & Design

The application follows a classic Layered Architecture pattern, separating concerns into distinct packages. This ensures that the user interface, business logic, and data access mechanisms are decoupled, making the system easier to test, maintain, and extend.

### Layered Architecture Diagram

```text
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

### Key Design Patterns Used
1. **Dependency Injection (DI)**: Components do not create their own dependencies. For example, `LibraryConsoleApp` receives its `LibraryService` and `ConsoleInputHelper` via its constructor. This promotes loose coupling and greatly simplifies unit testing via mocking.
2. **Strategy Pattern**: The `DataStore` interface allows the persistence mechanism to be swapped out without altering the core service logic. The `LibraryService` depends on the `DataStore` abstraction, not the concrete `JsonDataStore`.
3. **Single Responsibility Principle (SRP)**: Each class is designed to have exactly one reason to change. The `ConsoleInputHelper` handles only input parsing, `LibraryService` manages only business logic, and `Book` strictly models the domain data.
4. **Optional Pattern**: Instead of returning `null` when a book is not found, methods like `findById(int)` return an `Optional<Book>`. This forces the caller to explicitly handle the absence of a value, reducing `NullPointerException` risks.
5. **Custom Exceptions**: Domain-specific errors are modeled using custom unchecked exceptions (`BookNotFoundException`, `DuplicateBookException`), providing clear error semantics.

## 3. Technology Stack

The project relies entirely on standard Java libraries and standard build tools, maintaining a lean dependency footprint.

- **Language**: Java 17+ (Project compiled, tested, and optimized for JDK 21).
- **Build Tool**: Maven 3.x. The project includes the Maven Wrapper (`mvnw` and `mvnw.cmd`) to ensure a consistent build environment across different machines without requiring a global Maven installation.
- **Testing Framework**: JUnit Jupiter (JUnit 5) version 5.10.2 for comprehensive unit testing.
- **Dependencies**: No external runtime libraries are used. JSON serialization, data structures, and I/O rely exclusively on the `java.base` module.

## 4. Prerequisites & Environment Setup

Before working on the codebase, ensure your local development environment meets the following requirements:

- **Java Development Kit (JDK)**: JDK 17 or higher (JDK 21 recommended). Ensure the `JAVA_HOME` environment variable is set and points to your JDK installation.
- **Maven**: While the Maven Wrapper is provided, having Maven 3.8+ installed globally can be helpful for IDE integration.
- **IDE**: Any modern Java IDE is supported.
  - **IntelliJ IDEA**: Highly recommended. Provides excellent out-of-the-box support for Maven and JUnit 5.
  - **Eclipse / VS Code**: Fully supported via standard Maven import and Java extension packs.

## 5. Getting Started

Follow these steps to clone, build, and run the application locally.

### Cloning the Repository
```bash
git clone <repository-url>
cd Library_Book_Inventory_Week3
```

### Building the Project
Use the provided build scripts or the Maven Wrapper to compile the project, run tests, and package the application into an executable JAR.

**On Windows:**
```cmd
.\build.bat
# Or using Maven Wrapper directly:
.\mvnw.cmd clean package
```

**On macOS / Linux:**
```bash
./build.sh
# Or using Maven Wrapper directly:
./mvnw clean package
```

### Running the Application
Once built, the executable JAR is located in the `target/` directory.

**On Windows:**
```cmd
.\run.bat
```

**On macOS / Linux:**
```bash
./run.sh
```
*Alternatively, run the JAR directly:*
```bash
java -jar target/Library_Book_Inventory_Week3-1.0-SNAPSHOT.jar
```

## 6. Module Deep-Dive

The source code is organized into feature-centric packages.

### `library.Main`
The entry point of the application. It acts as the "composition root," responsible for bootstrapping the application. It wires the dependencies together (instantiating the data store, the service, the input helpers, and the UI) and optionally seeds initial sample data if the data store is empty.

### `library.model`
Contains the domain entities.
- **`Book.java`**: The core domain model representing a library book. 
  - **Fields**: `id` (int, immutable identity), `title` (String), `author` (String), `isbn` (String), `publicationYear` (int).
  - **Responsibilities**: Enforces basic field validation (e.g., non-blank strings, valid publication years). Implements `equals()` and `hashCode()` strictly based on the unique `id`.

### `library.service`
Contains the business logic layer.
- **`LibraryService.java`**: The heart of the application logic.
  - **Data Structures**: Utilizes a dual-index approach using two `HashMap`s (one mapping `id` to `Book`, another mapping `isbn` to `Book`). This guarantees O(1) time complexity for lookups by both ID and ISBN.
  - **Operations**: `addBook`, `getAllBooks`, `findById`, `search` (O(n) iteration for substring matching), `updateBook`, and `deleteBook`.
  - **Validation**: Enforces business rules, such as throwing `DuplicateBookException` if a new book's ISBN already exists.
  - **Persistence Binding**: Delegates to the `DataStore` to save changes automatically after any mutation.

### `library.exception`
Defines domain-specific error handling.
- **`BookNotFoundException.java`**: Thrown when operations (like update or delete) attempt to act on an ID that does not exist.
- **`DuplicateBookException.java`**: Thrown when adding or updating a book violates the unique ISBN constraint or unique ID constraint.

### `library.persistence`
Handles data storage and retrieval.
- **`DataStore.java`**: An interface defining `load()` and `save(List<Book>)` contracts.
- **`JsonDataStore.java`**: A concrete implementation of `DataStore` that reads and writes `Book` objects to a local file (`library_data.json`). Uses custom lightweight JSON parsing built on standard Java I/O to avoid external dependencies.

### `library.ui`
Manages user interactions via the console.
- **`ConsoleInputHelper.java`**: A utility class providing robust methods for reading user input (`readInt()`, `readString()`, `readYear()`). It handles `InputMismatchException` and prevents scanner buffer issues.
- **`LibraryConsoleApp.java`**: The main presentation logic. Displays the interactive menu, routes user choices to the appropriate `LibraryService` methods, and formats the output (e.g., printing tables of books).

## 7. Data Flow

Understanding the lifecycle of a request is crucial for maintenance. Here is the flow when a user adds a new book:

1. **User Input (UI)**: The user selects "1. Add Book" in the `LibraryConsoleApp`. The `ConsoleInputHelper` prompts the user for title, author, ISBN, and year, validating the raw input formats.
2. **Object Creation (UI/Model)**: `LibraryConsoleApp` instantiates a new `Book` object. The `Book` constructor performs domain validation (e.g., ensuring the year is >= 1000).
3. **Service Processing (Service)**: The `Book` is passed to `LibraryService.addBook(Book)`. The service checks the dual-index maps to ensure the ID and ISBN do not already exist. If unique, the book is added to the in-memory maps.
4. **Persistence Trigger (Service)**: Upon successful addition to memory, the service calls `dataStore.save(allBooksList)`.
5. **Disk Write (Persistence)**: The `JsonDataStore` serializes the list of books to JSON and overwrites `library_data.json`, ensuring the data is durably stored.
6. **User Feedback (UI)**: Control returns to `LibraryConsoleApp`, which prints a success message to the console.

## 8. Data Persistence

The application employs a simple but effective persistence strategy using JSON files.

- **Mechanism**: The `JsonDataStore` serializes the in-memory state into a human-readable JSON array.
- **File Location**: By default, it writes to `library_data.json` in the current working directory from which the application is executed.
- **Auto-Save**: The system uses an "auto-save on mutation" strategy. Any call to `addBook`, `updateBook`, or `deleteBook` immediately triggers a synchronous write to the disk.
- **Error Handling**: If the persistence layer fails (e.g., due to file permission issues or a full disk), an unchecked `RuntimeException` wraps the underlying `IOException`. In a production environment, this would be logged.
- **Initialization**: On application startup, `LibraryService` requests the `DataStore` to load existing data. If `library_data.json` does not exist, an empty list is returned, and a new file is created upon the first mutation.

## 9. Deployment Guide

Deploying the Library Book Inventory application is straightforward due to its packaged nature.

### Packaging
The application is packaged as a "fat" executable JAR (Java ARchive). The `pom.xml` uses the `maven-jar-plugin` to specify `library.Main` as the `Main-Class`.

### Deployment Steps
1. **Build the Artifact**: Run `./build.sh` or `./mvnw clean package`.
2. **Locate Artifact**: Retrieve `target/Library_Book_Inventory_Week3-1.0-SNAPSHOT.jar`.
3. **Transfer**: Copy the JAR file to the target production server or user's machine.
4. **Execution**: Execute the JAR using `java -jar <jar-file-name>`.

### Production Considerations
- **Data File Location**: In a production setting, relying on the current working directory for `library_data.json` is risky. It is recommended to modify `Main.java` to inject an absolute path (e.g., `/var/lib/libraryapp/data.json`) into the `JsonDataStore` constructor via an environment variable.
- **Headless Environment**: Ensure the server running the application provides a pseudo-terminal if being interacted with directly, as it requires `System.in`.

## 10. Testing

The application features a comprehensive test suite prioritizing business logic reliability.

### Running Tests
Tests are executed automatically during the Maven `package` phase. To run them explicitly:
```bash
./mvnw test
```

### Test Organization
- **Location**: `src/test/java/library/service/`
- **Focus**: The primary test class is `LibraryServiceTest.java` (containing 22 tests).
- **Structure**: Tests use JUnit 5's `@Nested` annotation to group related test cases (e.g., `AddBookTests`, `SearchTests`, `UpdateTests`). The `@DisplayName` annotation is heavily utilized to provide readable output in test reports.
- **Coverage**:
  - Valid CRUD operations.
  - Rejection of duplicate IDs and ISBNs.
  - Case-insensitive search behavior.
  - Model validation constraints.
  - `equals()` and `hashCode()` contract compliance.

### Adding New Tests
When extending the application, add corresponding tests in the `src/test/java` directory. If modifying `LibraryService`, ensure you mock the `DataStore` (or provide a stub, as is currently done) to prevent tests from performing actual disk I/O, maintaining fast test execution.

## 11. Extending the Application

The system's modular design makes extensions straightforward. Here are common scenarios for future developers:

### Scenario 1: Adding a New Entity (e.g., Patron)
1. **Model**: Create `library.model.Patron.java` with validation.
2. **Service**: Create `library.service.PatronService.java` for logic.
3. **DataStore**: Update `DataStore` to handle generic types, or create a specific `PatronDataStore`.
4. **UI**: Update `LibraryConsoleApp` menu to include "Manage Patrons", delegating to a new `PatronConsoleApp` module.

### Scenario 2: Swapping to Database Persistence
1. **Dependency**: Add a JDBC driver (e.g., PostgreSQL or MySQL) to `pom.xml`.
2. **Implementation**: Create `library.persistence.JdbcDataStore implements DataStore`. Implement `load` (SELECT queries) and `save` (INSERT/UPDATE queries).
3. **Wiring**: In `Main.java`, instantiate `JdbcDataStore` instead of `JsonDataStore` and pass it to the `LibraryService`. The rest of the application remains completely untouched.

### Scenario 3: Adding a REST API
1. **Framework**: Migrate the project to Spring Boot or Javalin.
2. **Controllers**: Create a `library.controller` package. Expose HTTP endpoints (GET `/api/books`, POST `/api/books`).
3. **Wiring**: Inject the existing `LibraryService` into the new controllers.
4. **UI Phase Out**: Deprecate or remove the `library.ui` package.

## 12. Configuration & Customization

Currently, the application relies on hardcoded configurations inside `Main.java` for simplicity.

- **Data File Location**: The `library_data.json` path is defined when instantiating `JsonDataStore`.
- **Sample Data**: The `Main.java` file contains a logic block that seeds the database with 3 sample books if the data store is empty. To customize this, modify the `LibraryService.addBook` calls within the `Main` class.

*Future Enhancement*: Implement a `config.properties` file reader to load settings dynamically at runtime.

## 13. Troubleshooting

Here are common issues developers or users might encounter and how to resolve them:

| Issue / Symptom | Possible Cause | Resolution |
| :--- | :--- | :--- |
| **`UnsupportedClassVersionError` on startup** | Executing JAR with an older Java version (e.g., Java 11) than it was compiled with (Java 17/21). | Ensure `java -version` returns 17 or higher. Update the `PATH` or `JAVA_HOME`. |
| **Changes not saving between sessions** | `JsonDataStore` lacks write permissions in the execution directory. | Run the application in a directory where the user has write access, or run as administrator/root. |
| **`InputMismatchException` looping continuously** | Scanner buffer not cleared after an invalid numeric input. | Handled internally by `ConsoleInputHelper`. If adding new input methods, ensure `scanner.nextLine()` is called in the `catch` block to consume the invalid token. |
| **Tests fail on Windows but pass on Linux** | File path hardcoding in tests. | Ensure tests use in-memory stubs or relative paths utilizing `File.separator`. (Current tests use stubs, avoiding this issue). |

## 14. Maintenance & Scalability Strategy

As the application grows, consider the following roadmap:

- **Logging**: Replace `System.out.println` and `e.printStackTrace()` with a robust logging framework like SLF4J and Logback. This is crucial for production monitoring.
- **Scalability**: The current in-memory `HashMap` architecture guarantees O(1) performance and is incredibly fast. However, it requires the entire dataset to fit in RAM. For millions of books, migrate to a relational database using `JdbcDataStore`.
- **Concurrency**: `LibraryService` is not currently thread-safe. If moving to a multi-threaded web environment, `HashMap` must be replaced with `ConcurrentHashMap`, and state mutations must be synchronized or handled transactionally by a database.
- **Versioning**: Adhere to Semantic Versioning (SemVer). Update versions in the `pom.xml` before creating release tags in source control.

## 15. Appendix: Version History

A summary of the project's evolution:

- **Week 1: Core Domain** 
  - Designed the `Book` model.
  - Implemented basic input handling and console structure.
- **Week 2: Basic Service Layer**
  - Created initial `LibraryService` using an `ArrayList`.
  - Implemented rudimentary add/view/delete functionality.
- **Week 3: Refinement & Testing**
  - Identified performance bottlenecks.
  - Wrote initial unit tests for core functionality.
- **Week 4: Major Architecture Refactoring**
  - Replaced `ArrayList` with O(1) Dual-Index `HashMap`.
  - Introduced Dependency Injection and `Optional` pattern.
  - Created custom exceptions (`BookNotFoundException`, `DuplicateBookException`).
- **Week 5: Persistence & Deployment**
  - Implemented Strategy pattern with `DataStore` interface.
  - Wrote `JsonDataStore` for file-based persistence.
  - Added Maven wrappers and created shell scripts for simplified deployment.
  - Finalized developer documentation.

---
*End of Developer Documentation.*
