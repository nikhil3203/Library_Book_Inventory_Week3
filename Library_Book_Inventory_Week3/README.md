# Library Book Inventory Management System

**Version:** 3.0.0 (Week 5 — Final Integrated Release)  
**Java:** 17+ (tested with JDK 21)  
**Build:** Maven 3.x or standalone `javac` via included build scripts

---

## Overview

A console-based library book inventory management system developed as a Java internship project (Weeks 1–5). The application supports full CRUD (Create, Read, Update, Delete) operations for managing a book catalog, with file-based JSON persistence so data survives application restarts.

## Quick Start

### Option 1: Build Scripts (No Maven Required)

```bash
# Windows
build.bat
run.bat

# Unix / macOS
chmod +x build.sh run.sh
./build.sh
./run.sh
```

### Option 2: Maven

```bash
mvn clean package
java -jar target/library-book-inventory.jar
```

## Features

| Feature | Description |
|---------|-------------|
| **Add Book** | Add a new book with ID, title, author, ISBN, and year |
| **View All** | List all books in the inventory |
| **Update Book** | Modify any field of an existing book |
| **Delete Book** | Remove a book by ID |
| **Search** | Case-insensitive search by title or author |
| **Persistence** | Auto-saves to `library_data.json` after every change |

## Project Structure

```
library/
├── Main.java                          # Entry point — wires dependencies
├── model/
│   └── Book.java                      # Domain model with validation
├── service/
│   └── LibraryService.java            # CRUD operations (O(1) lookups)
├── persistence/
│   ├── DataStore.java                 # Strategy interface
│   └── JsonDataStore.java             # JSON file persistence
├── exception/
│   ├── BookNotFoundException.java     # Custom exception
│   └── DuplicateBookException.java    # Custom exception
└── ui/
    ├── LibraryConsoleApp.java         # Menu-driven console UI
    └── ConsoleInputHelper.java        # Input utilities
```

## Development History

| Week | Focus | Key Deliverables |
|------|-------|------------------|
| 1–3 | Core development | Book model, LibraryService, console UI, bug fixes, 17 unit tests |
| 4 | Refactoring | HashMap optimization (O(1)), package restructuring, custom exceptions, validation, DI, Optional pattern, 22 tests |
| 5 | Integration & Deployment | File persistence, deployment scripts, executable JAR, developer documentation |

## Documentation

For comprehensive developer documentation including architecture, deployment, extension guide, and maintenance strategy, see [Developer_Documentation.md](Developer_Documentation.md).

For the Week 4 refactoring report, see [Week4_Refactoring_Report.md](Week4_Refactoring_Report.md).

For the Week 5 integration, deployment, and documentation report, see [Week5_Integration_Report.md](Week5_Integration_Report.md).

## Testing

```bash
# Run all 22 unit tests
mvn test
```

## License

Internship project — for educational purposes.
