package library.service;

import library.exception.BookNotFoundException;
import library.exception.DuplicateBookException;
import library.model.Book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LibraryService}.
 *
 * <p><b>Week 4 Refactoring — changes from Week 3 test suite:</b></p>
 * <ul>
 *   <li>Updated to {@code library.service} package (matches new package layout).</li>
 *   <li>Tests now assert custom exception types ({@link BookNotFoundException},
 *       {@link DuplicateBookException}) instead of generic
 *       {@code IllegalArgumentException} — validates Issue #7 fix.</li>
 *   <li>{@code findById()} tests updated for {@code Optional} return type
 *       (Issue #8).</li>
 *   <li>Added {@link BookValidationTest} nested class for model-layer validation
 *       (Issue #3).</li>
 *   <li>All 17 original test cases preserved and passing.</li>
 * </ul>
 */
class LibraryServiceTest {

    private LibraryService service;

    @BeforeEach
    void setUp() {
        service = new LibraryService();
    }

    // ========================= CREATE =========================

    @Test
    @DisplayName("addBook stores a valid book and it can be retrieved")
    void testAddBook() {
        Book book = new Book(101, "Head First Java", "Kathy Sierra", "9780596009205", 2005);

        service.addBook(book);

        assertEquals(1, service.getAllBooks().size());
        assertTrue(service.findById(101).isPresent());
        assertSame(book, service.findById(101).get());
    }

    @Test
    @DisplayName("addBook rejects a duplicate ID with DuplicateBookException")
    void testAddBookDuplicateId() {
        service.addBook(new Book(101, "Java Basics", "James Gosling", "1111111111", 2020));
        Book duplicate = new Book(101, "Advanced Java", "James Gosling", "2222222222", 2021);

        // Week 4: now throws custom DuplicateBookException instead of IAE
        DuplicateBookException ex = assertThrows(DuplicateBookException.class,
                () -> service.addBook(duplicate));
        assertEquals("ID", ex.getField());
        assertEquals(1, service.getAllBooks().size(), "Rejected book must not be added");
    }

    @Test
    @DisplayName("addBook rejects a duplicate ISBN even with different casing")
    void testAddBookDuplicateIsbnCaseInsensitive() {
        service.addBook(new Book(101, "Java Basics", "Author One", "abc123XYZ", 2020));
        Book duplicate = new Book(102, "Advanced Java", "Author Two", "ABC123xyz", 2021);

        // Week 4: now throws custom DuplicateBookException
        DuplicateBookException ex = assertThrows(DuplicateBookException.class,
                () -> service.addBook(duplicate));
        assertEquals("ISBN", ex.getField());
    }

    // ========================= READ =========================

    @Test
    @DisplayName("getAllBooks returns a defensive copy, not the live collection")
    void testGetAllBooksReturnsCopy() {
        service.addBook(new Book(101, "Java Basics", "Author", "1234567890", 2020));

        List<Book> snapshot = service.getAllBooks();

        // Week 4: returned list is unmodifiable — attempting to clear throws
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertEquals(1, service.getAllBooks().size(),
                "Internal state must not be affected");
    }

    @Test
    @DisplayName("findById returns an Optional containing the matching book")
    void testFindById() {
        service.addBook(new Book(101, "Java Basics", "Author", "1234567890", 2020));

        // Week 4: returns Optional instead of nullable reference (Issue #8)
        Optional<Book> result = service.findById(101);

        assertTrue(result.isPresent());
        assertEquals(101, result.get().getId());
    }

    @Test
    @DisplayName("findById returns empty Optional when no book matches")
    void testFindByIdNotFound() {
        // Week 4: empty Optional instead of null (Issue #8)
        assertTrue(service.findById(999).isEmpty());
    }

    @Test
    @DisplayName("search matches by title, case-insensitively")
    void testSearchByTitle() {
        service.addBook(new Book(101, "Head First Java", "Kathy Sierra", "1234567890", 2005));

        List<Book> result = service.search("JAVA");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("search matches by author, case-insensitively")
    void testSearchByAuthor() {
        service.addBook(new Book(101, "Java Basics", "Kathy Sierra", "1234567890", 2005));

        List<Book> result = service.search("kathy");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("search returns an empty list, not null, when nothing matches")
    void testSearchNoMatches() {
        service.addBook(new Book(101, "Java Basics", "Kathy Sierra", "1234567890", 2005));

        List<Book> result = service.search("python");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Bug #2 (fixed in Week 3): search with a null keyword throws IllegalArgumentException")
    void testSearchNullKeyword() {
        assertThrows(IllegalArgumentException.class, () -> service.search(null));
    }

    // ========================= UPDATE =========================

    @Test
    @DisplayName("updateBook overwrites every field of an existing book")
    void testUpdateBook() {
        service.addBook(new Book(101, "Java Basics", "Author", "1234567890", 2020));

        service.updateBook(101, "Advanced Java", "New Author", "9999999999", 2024);

        Book updated = service.findById(101).orElseThrow();
        assertEquals("Advanced Java", updated.getTitle());
        assertEquals("New Author", updated.getAuthor());
        assertEquals("9999999999", updated.getIsbn());
        assertEquals(2024, updated.getPublicationYear());
    }

    @Test
    @DisplayName("updateBook allows a book to keep its own ISBN unchanged")
    void testUpdateBookKeepsOwnIsbn() {
        service.addBook(new Book(101, "Java Basics", "Author", "1234567890", 2020));

        assertDoesNotThrow(() ->
                service.updateBook(101, "Java Basics 2nd Ed.", "Author", "1234567890", 2021));
        assertEquals(2021, service.findById(101).orElseThrow().getPublicationYear());
    }

    @Test
    @DisplayName("updateBook rejects an ISBN that belongs to a different book")
    void testUpdateBookIsbnConflict() {
        service.addBook(new Book(101, "Java Basics", "Author", "1111111111", 2020));
        service.addBook(new Book(102, "Advanced Java", "Author", "2222222222", 2021));

        // Week 4: now throws DuplicateBookException
        assertThrows(DuplicateBookException.class,
                () -> service.updateBook(102, "Advanced Java", "Author", "1111111111", 2021));
    }

    @Test
    @DisplayName("updateBook throws BookNotFoundException when the target book does not exist")
    void testUpdateNonExistingBook() {
        // Week 4: now throws BookNotFoundException instead of IAE
        assertThrows(BookNotFoundException.class,
                () -> service.updateBook(999, "Java", "Author", "1234567890", 2024));
    }

    // ========================= DELETE =========================

    @Test
    @DisplayName("deleteBook removes the book from the inventory")
    void testDeleteBook() {
        service.addBook(new Book(101, "Java Basics", "Author", "1234567890", 2020));

        service.deleteBook(101);

        assertTrue(service.findById(101).isEmpty());
        assertEquals(0, service.getAllBooks().size());
    }

    @Test
    @DisplayName("deleteBook throws BookNotFoundException when the target book does not exist")
    void testDeleteNonExistingBook() {
        // Week 4: now throws BookNotFoundException instead of IAE
        assertThrows(BookNotFoundException.class, () -> service.deleteBook(999));
    }

    // ========================= BOOK VALIDATION (Week 4 — NEW) =========================

    /**
     * Tests for model-layer validation added in Week 4 (Issue #3).
     * These tests verify that the {@link Book} constructor and setters reject
     * invalid inputs, a protection that was completely missing in Week 3.
     */
    @Nested
    @DisplayName("Book model validation (Week 4 — Issue #3)")
    class BookValidationTest {

        @Test
        @DisplayName("Book rejects a non-positive ID")
        void testBookRejectsNonPositiveId() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Book(0, "Title", "Author", "ISBN", 2020));
            assertThrows(IllegalArgumentException.class,
                    () -> new Book(-1, "Title", "Author", "ISBN", 2020));
        }

        @Test
        @DisplayName("Book rejects a null or blank title")
        void testBookRejectsBlankTitle() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Book(1, null, "Author", "ISBN", 2020));
            assertThrows(IllegalArgumentException.class,
                    () -> new Book(1, "   ", "Author", "ISBN", 2020));
        }

        @Test
        @DisplayName("Book rejects a null or blank author")
        void testBookRejectsBlankAuthor() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Book(1, "Title", null, "ISBN", 2020));
            assertThrows(IllegalArgumentException.class,
                    () -> new Book(1, "Title", "  ", "ISBN", 2020));
        }

        @Test
        @DisplayName("Book rejects a null or blank ISBN")
        void testBookRejectsBlankIsbn() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Book(1, "Title", "Author", null, 2020));
            assertThrows(IllegalArgumentException.class,
                    () -> new Book(1, "Title", "Author", "", 2020));
        }

        @Test
        @DisplayName("Book rejects an out-of-range publication year")
        void testBookRejectsInvalidYear() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Book(1, "Title", "Author", "ISBN", 999));
            assertThrows(IllegalArgumentException.class,
                    () -> new Book(1, "Title", "Author", "ISBN", 9999));
        }

        @Test
        @DisplayName("Book equals/hashCode is based on ID")
        void testBookEquality() {
            Book a = new Book(1, "Title A", "Author A", "ISBN-A", 2020);
            Book b = new Book(1, "Title B", "Author B", "ISBN-B", 2021);
            Book c = new Book(2, "Title A", "Author A", "ISBN-A", 2020);

            assertEquals(a, b, "Same ID → equal");
            assertNotEquals(a, c, "Different ID → not equal");
            assertEquals(a.hashCode(), b.hashCode(), "Same ID → same hashCode");
        }
    }
}
