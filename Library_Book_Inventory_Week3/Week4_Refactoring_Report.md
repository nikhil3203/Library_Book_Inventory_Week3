# Week 4 — Code Refactoring and Optimization Report

**Project:** Library Book Inventory Management System  
**Author:** Internship — Junior Java Developer  
**Date:** September 2026  
**Version:** 1.0.0 → 2.0.0

---

## 1. Executive Summary

This report documents the comprehensive refactoring of the Library Book Inventory Management System originally developed during Weeks 2–3 of the internship. The refactoring focused on **nine identified issues** spanning performance bottlenecks, DRY violations, missing validation, architectural concerns, and idiomatic Java usage. All changes were verified with an expanded test suite of **22 passing tests** (up from 17 in Week 3).

### Key Achievements
| Metric | Before (Week 3) | After (Week 4) |
|--------|-----------------|-----------------|
| Source files | 3 | 7 |
| Packages | 1 (`library`) | 4 (`model`, `service`, `ui`, `exception`) |
| Test cases | 17 | 22 |
| Lookup complexity | O(n) | **O(1)** |
| Custom exception types | 0 | 2 |
| Input validation in model | None | Full (null/blank/range) |

---

## 2. Issues Identified in Code Review

| # | Issue | Category | File |
|---|-------|----------|------|
| 1 | O(n) linear scan for every ID lookup | Performance | `LibraryService.java` |
| 2 | Duplicated ISBN uniqueness loops in `addBook()` and `updateBook()` | DRY / Performance | `LibraryService.java` |
| 3 | No input validation — null/blank fields silently accepted | Robustness | `Book.java` |
| 4 | Missing `equals()` / `hashCode()` — undefined identity semantics | Correctness | `Book.java` |
| 5 | All classes in a single flat package | Architecture | All files |
| 6 | `Main.java` mixing UI, parsing, orchestration, and bootstrapping | SRP | `Main.java` |
| 7 | Generic `IllegalArgumentException` for all domain errors | Design Pattern | `LibraryService.java` |
| 8 | `findById()` returns `null` — forces null-checks on every caller | Idiomatic Java | `LibraryService.java` |
| 9 | `Book.update()` mutates fields without validation | Encapsulation | `Book.java` |

---

## 3. Refactoring Changes — Before & After

### 3.1 Data Structure Optimization (Issue #1, #2)

**Problem:** `LibraryService` stored books in an `ArrayList<Book>`. Every `findById()`, `addBook()`, `updateBook()`, and `deleteBook()` call performed an O(n) linear scan. ISBN uniqueness also required a separate O(n) scan.

**BEFORE (Week 3):**
```java
public class LibraryService {
    private final List<Book> books = new ArrayList<>();

    // O(n) — scans entire list every time
    public Book findById(int id) {
        for (Book book : books) {
            if (book.getId() == id) return book;
        }
        return null;
    }

    public void addBook(Book book) {
        if (findById(book.getId()) != null) { ... }      // O(n) scan #1
        for (Book existing : books) {                     // O(n) scan #2
            if (existing.getIsbn().equalsIgnoreCase(book.getIsbn())) { ... }
        }
        books.add(book);
    }

    public void deleteBook(int id) {
        Book book = findById(id);    // O(n) scan
        books.remove(book);          // O(n) scan again
    }
}
```

**AFTER (Week 4):**
```java
public class LibraryService {
    // Dual-index: O(1) for both ID and ISBN lookups
    private final Map<Integer, Book> booksById = new HashMap<>();
    private final Map<String, Integer> isbnToId = new HashMap<>();

    // O(1) — direct HashMap lookup
    public Optional<Book> findById(int id) {
        return Optional.ofNullable(booksById.get(id));
    }

    public void addBook(Book book) {
        if (booksById.containsKey(book.getId())) { ... }  // O(1)
        validateIsbnUnique(book.getIsbn(), -1);            // O(1) via isbnToId
        booksById.put(book.getId(), book);
        isbnToId.put(book.getIsbn().toLowerCase(), book.getId());
    }

    public void deleteBook(int id) {
        Book book = booksById.remove(id);  // O(1) lookup + remove in one call
        isbnToId.remove(book.getIsbn().toLowerCase());
    }
}
```

**Performance Comparison:**
| Operation | Week 3 | Week 4 | Improvement |
|-----------|--------|--------|-------------|
| `findById(id)` | O(n) | O(1) | n× faster |
| `addBook()` — ID check | O(n) | O(1) | n× faster |
| `addBook()` — ISBN check | O(n) | O(1) | n× faster |
| `updateBook()` — find + ISBN check | O(n) + O(n) | O(1) + O(1) | n× faster |
| `deleteBook()` — find + remove | O(n) + O(n) | O(1) | n× faster |
| `search(keyword)` | O(n) | O(n) | Unchanged (inherently requires full scan) |

---

### 3.2 DRY — ISBN Validation Helper (Issue #2)

**Problem:** The ISBN uniqueness check was duplicated as nearly identical loops in `addBook()` and `updateBook()`.

**BEFORE (Week 3):**
```java
// In addBook():
for (Book existing : books) {
    if (existing.getIsbn().equalsIgnoreCase(book.getIsbn())) {
        throw new IllegalArgumentException("ISBN already exists.");
    }
}

// In updateBook() — almost identical loop:
for (Book existing : books) {
    if (existing != book && existing.getIsbn().equalsIgnoreCase(isbn)) {
        throw new IllegalArgumentException("ISBN already belongs to another book.");
    }
}
```

**AFTER (Week 4):**
```java
// Single shared helper — both addBook() and updateBook() delegate here
private void validateIsbnUnique(String isbn, int excludeId) {
    Integer existingId = isbnToId.get(isbn.toLowerCase());
    if (existingId != null && existingId != excludeId) {
        throw new DuplicateBookException("ISBN", isbn);
    }
}

// In addBook():
validateIsbnUnique(book.getIsbn(), -1);  // -1 = no exclusion (new book)

// In updateBook():
validateIsbnUnique(isbn, id);            // exclude the book being updated
```

**Improvement:** Validation logic is defined once and reused. If the business rule changes (e.g., ISBN format validation), only one method needs updating.

---

### 3.3 Model Validation & Identity (Issue #3, #4, #9)

**Problem:** `Book` accepted any input without validation — null titles, blank ISBNs, negative years all silently created invalid objects. Additionally, no `equals()`/`hashCode()` meant undefined identity semantics.

**BEFORE (Week 3):**
```java
public class Book {
    public Book(int id, String title, String author, String isbn, int publicationYear) {
        this.id = id;
        this.title = title;       // could be null — no check
        this.author = author;     // could be null — no check
        this.isbn = isbn;         // could be blank — no check
        this.publicationYear = publicationYear;  // could be -1 — no check
    }

    public void update(String title, String author, String isbn, int year) {
        this.title = title;       // bypasses any validation
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = year;
    }

    // No equals() or hashCode() — identity is reference-based
}
```

**AFTER (Week 4):**
```java
public class Book {
    public Book(int id, String title, String author, String isbn, int publicationYear) {
        if (id <= 0) throw new IllegalArgumentException("Book ID must be positive.");
        this.id = id;
        setTitle(title);            // validates non-blank
        setAuthor(author);          // validates non-blank
        setIsbn(isbn);              // validates non-blank
        setPublicationYear(publicationYear);  // validates range [1000, current year]
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Title cannot be null or blank.");
        this.title = title.trim();
    }
    // ... similar setters for author, isbn, publicationYear

    public void update(String title, String author, String isbn, int year) {
        setTitle(title);    // DRY: re-uses validated setters
        setAuthor(author);
        setIsbn(isbn);
        setPublicationYear(year);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return id == book.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
```

**Improvement:** Invalid objects can never be constructed. The `equals()`/`hashCode()` contract means `Book` works correctly in `HashMap`, `HashSet`, and equality assertions.

---

### 3.4 Custom Exceptions (Issue #7)

**Problem:** All domain errors used `IllegalArgumentException`, making it impossible for callers to distinguish between "book not found," "duplicate ID," and "duplicate ISBN" without parsing message strings.

**BEFORE (Week 3):**
```java
// All errors look the same to catch blocks:
throw new IllegalArgumentException("Book ID already exists.");
throw new IllegalArgumentException("ISBN already exists.");
throw new IllegalArgumentException("Book not found.");
```

**AFTER (Week 4):**
```java
// Each error is a distinct, catchable type:
throw new DuplicateBookException("ID", String.valueOf(book.getId()));
throw new DuplicateBookException("ISBN", isbn);
throw new BookNotFoundException(id);

// Callers can now react specifically:
try {
    library.addBook(book);
} catch (DuplicateBookException ex) {
    if ("ISBN".equals(ex.getField())) {
        // handle ISBN conflict differently
    }
} catch (BookNotFoundException ex) {
    log.warn("Book {} not found", ex.getBookId());
}
```

---

### 3.5 Optional Pattern (Issue #8)

**Problem:** `findById()` returned `null`, requiring every caller to add a null-check or risk a `NullPointerException`.

**BEFORE (Week 3):**
```java
public Book findById(int id) {
    for (Book book : books) {
        if (book.getId() == id) return book;
    }
    return null;  // caller must remember to null-check
}

// Caller code:
if (library.findById(id) == null) {
    System.out.println("Book not found.");
    return;
}
```

**AFTER (Week 4):**
```java
public Optional<Book> findById(int id) {
    return Optional.ofNullable(booksById.get(id));
}

// Caller code — compiler enforces handling:
if (library.findById(id).isEmpty()) {
    System.out.println("Book not found.");
    return;
}

// Or fluent style:
library.findById(id)
       .ifPresentOrElse(
           book -> System.out.println(book),
           () -> System.out.println("Not found")
       );
```

---

### 3.6 SRP / Modularization (Issue #5, #6)

**Problem:** `Main.java` was 153 lines combining application bootstrapping, menu display, input parsing, and business-flow orchestration in static methods. All 3 classes lived in a single package.

**BEFORE (Week 3):**
```
library/
  Book.java              (model + UI data in same package)
  LibraryService.java    (service logic mixed with models)
  Main.java              (153 lines: bootstrap + menu + input parsing + orchestration)
```

**AFTER (Week 4):**
```
library/
  Main.java              (~50 lines: bootstrap only — wires dependencies)
  exception/
    BookNotFoundException.java
    DuplicateBookException.java
  model/
    Book.java            (validated domain model)
  service/
    LibraryService.java  (pure business logic)
  ui/
    ConsoleInputHelper.java   (reusable input utilities)
    LibraryConsoleApp.java    (menu orchestration with DI)
```

**Key design decisions:**
- **`ConsoleInputHelper`** accepts a `Scanner` via its constructor → testable with `new Scanner("test input")`.
- **`LibraryConsoleApp`** accepts `LibraryService` and `ConsoleInputHelper` via its constructor → **Dependency Injection** enables easy unit testing with mocks.
- **`Main.java`** is now a thin wiring layer: it constructs dependencies and calls `app.run()`.

---

## 4. Refactoring Techniques Summary

| Technique | Where Applied | Impact |
|-----------|---------------|--------|
| **DRY** (Don't Repeat Yourself) | ISBN validation helper; Book constructor delegates to setters | Eliminated duplicated code; single source of truth for validation |
| **SRP** (Single Responsibility) | Split `Main.java` → 3 classes; package-by-layer structure | Each class has one reason to change |
| **Data-structure optimization** | `ArrayList` → `HashMap` + ISBN index | O(n) → O(1) for all lookups |
| **Custom exceptions** | `BookNotFoundException`, `DuplicateBookException` | Programmatic error distinction; self-documenting API |
| **Defensive programming** | Constructor/setter validation in `Book` | Invalid objects can never be created |
| **Optional pattern** | `findById()` returns `Optional<Book>` | Compile-time enforcement of null handling |
| **Dependency Injection** | `LibraryConsoleApp` receives dependencies via constructor | Testable, loosely coupled components |
| **Package-by-layer** | `model`, `service`, `ui`, `exception` sub-packages | Clear separation of concerns |

---

## 5. Test Results

### Test Execution: 22/22 PASSED ✓

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

- **16 existing tests** were updated for new packages and exception types
- **6 new tests** added for model-layer validation (Issue #3) and identity (Issue #4)

---

## 6. Conclusion

The refactoring addressed all 9 identified issues, resulting in a codebase that is:

1. **Faster** — HashMap-backed lookups deliver O(1) performance for the most frequent operations.
2. **More robust** — model-layer validation prevents invalid data from ever entering the system.
3. **More maintainable** — package-by-layer structure, DRY validation, and SRP decomposition make each component independently understandable and modifiable.
4. **More expressive** — custom exceptions and Optional returns make the API self-documenting and safer to use.
5. **More testable** — Dependency Injection and extracted utilities enable isolated unit testing of each component.

All 22 tests pass successfully, confirming that the refactoring preserved existing behavior while adding new safety guarantees.
